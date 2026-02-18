#!/usr/bin/env python3
"""
Setup Aliyun OSS bucket for Manul package distribution
- Creates OSS bucket
- Binds custom domain pkg.metavm.tech
- Enables transfer acceleration
- Configures DNS (Aliyun DNS)
"""

import os
import sys
import json

try:
    import oss2
    from aliyunsdkcore.client import AcsClient
    from aliyunsdkcore.request import CommonRequest
except ImportError:
    print("Installing required packages...")
    os.system("pip3 install aliyun-python-sdk-core oss2 aliyun-python-sdk-alidns")
    import oss2
    from aliyunsdkcore.client import AcsClient
    from aliyunsdkcore.request import CommonRequest

# Load credentials
creds_file = "/Users/leen/develop/env/aliyun.properties"
creds = {}
with open(creds_file) as f:
    for line in f:
        if '=' in line:
            key, value = line.strip().split('=', 1)
            creds[key] = value

ACCESS_KEY_ID = creds['ALIYUN_ACCESS_KEY_ID']
ACCESS_KEY_SECRET = creds['ALIYUN_ACCESS_KEY_SECRET']

# Configuration
BUCKET_NAME = 'manul-packages'
REGION = 'oss-cn-hongkong'  # Hong Kong region for better international access
DOMAIN = 'pkg.metavm.tech'
ENDPOINT = f'https://{REGION}.aliyuncs.com'

def create_bucket():
    """Create OSS bucket"""
    print(f"\n==> Creating OSS bucket: {BUCKET_NAME}")

    auth = oss2.Auth(ACCESS_KEY_ID, ACCESS_KEY_SECRET)
    bucket = oss2.Bucket(auth, ENDPOINT, BUCKET_NAME)

    try:
        # Create bucket - will use default ACL
        bucket.create_bucket()
        print(f"✓ Bucket created: {BUCKET_NAME}")

        # Set bucket ACL to public-read
        try:
            bucket.put_bucket_acl(oss2.BUCKET_ACL_PUBLIC_READ)
            print(f"✓ Bucket ACL set to public-read")
        except Exception as e:
            print(f"! Could not set public ACL (not critical): {e}")

    except Exception as e:
        # Bucket already exists
        print(f"! Bucket already exists or owned by you: {BUCKET_NAME}")

    return bucket

def enable_transfer_acceleration(bucket):
    """Enable transfer acceleration"""
    print(f"\n==> Enabling transfer acceleration")

    try:
        # Enable transfer acceleration using the correct API
        from oss2.models import BucketTransferAccelerationConfiguration
        config = BucketTransferAccelerationConfiguration(enabled=True)
        bucket.put_bucket_transfer_acceleration(config)
        print(f"✓ Transfer acceleration enabled")

        # Get accelerated endpoint
        accelerated_endpoint = f"{BUCKET_NAME}.oss-accelerate.aliyuncs.com"
        print(f"✓ Accelerated endpoint: {accelerated_endpoint}")
        return accelerated_endpoint
    except Exception as e:
        print(f"✗ Failed to enable transfer acceleration: {e}")
        print(f"! Continuing with standard endpoint")
        return None

def bind_domain(bucket):
    """Bind custom domain to bucket"""
    print(f"\n==> Binding domain: {DOMAIN}")

    # Get CNAME target
    cname_target = f"{BUCKET_NAME}.{REGION}.aliyuncs.com"

    try:
        # Bind CNAME using correct model
        from oss2.models import PutBucketCnameRequest, CnameInfo
        cname_info = CnameInfo(domain=DOMAIN)
        request = PutBucketCnameRequest(cname_info)
        bucket.put_bucket_cname(request)
        print(f"✓ Domain bound: {DOMAIN}")
        print(f"✓ CNAME target: {cname_target}")
        return cname_target
    except Exception as e:
        print(f"✗ Failed to bind domain via API: {e}")
        print(f"\nManual domain binding required:")
        print(f"  1. Go to Aliyun OSS Console")
        print(f"  2. Navigate to bucket: {BUCKET_NAME}")
        print(f"  3. Go to Domain Management")
        print(f"  4. Add custom domain: {DOMAIN}")
        return cname_target

def configure_dns(cname_target):
    """Configure Aliyun DNS"""
    print(f"\n==> Configuring DNS for {DOMAIN}")

    client = AcsClient(ACCESS_KEY_ID, ACCESS_KEY_SECRET, 'cn-hangzhou')

    # Extract domain name (metavm.tech)
    domain_name = '.'.join(DOMAIN.split('.')[-2:])
    subdomain = DOMAIN.split('.')[0]

    try:
        # Check if record exists
        request = CommonRequest()
        request.set_domain('alidns.aliyuncs.com')
        request.set_version('2015-01-09')
        request.set_action_name('DescribeDomainRecords')
        request.add_query_param('DomainName', domain_name)
        request.add_query_param('RRKeyWord', subdomain)

        response = client.do_action_with_exception(request)
        result = json.loads(response)

        if result['TotalCount'] > 0:
            # Update existing record
            record_id = result['DomainRecords']['Record'][0]['RecordId']
            print(f"! DNS record exists, updating: {record_id}")

            request = CommonRequest()
            request.set_domain('alidns.aliyuncs.com')
            request.set_version('2015-01-09')
            request.set_action_name('UpdateDomainRecord')
            request.add_query_param('RecordId', record_id)
            request.add_query_param('RR', subdomain)
            request.add_query_param('Type', 'CNAME')
            request.add_query_param('Value', cname_target)

            client.do_action_with_exception(request)
            print(f"✓ DNS record updated")
        else:
            # Create new record
            request = CommonRequest()
            request.set_domain('alidns.aliyuncs.com')
            request.set_version('2015-01-09')
            request.set_action_name('AddDomainRecord')
            request.add_query_param('DomainName', domain_name)
            request.add_query_param('RR', subdomain)
            request.add_query_param('Type', 'CNAME')
            request.add_query_param('Value', cname_target)

            client.do_action_with_exception(request)
            print(f"✓ DNS record created")

        print(f"✓ DNS configured: {DOMAIN} -> {cname_target}")

    except Exception as e:
        print(f"✗ Failed to configure DNS: {e}")
        print(f"\nManual DNS configuration required:")
        print(f"  Record Type: CNAME")
        print(f"  Host: {subdomain}")
        print(f"  Value: {cname_target}")

def set_cors_policy(bucket):
    """Set CORS policy for web access"""
    print(f"\n==> Configuring CORS policy")

    try:
        rule = oss2.models.CorsRule(
            allowed_origins=['*'],
            allowed_methods=['GET', 'HEAD'],
            allowed_headers=['*'],
            max_age_seconds=3600
        )
        bucket.put_bucket_cors(oss2.models.BucketCors([rule]))
        print(f"✓ CORS policy configured")
    except Exception as e:
        print(f"✗ Failed to configure CORS: {e}")

def create_directory_structure(bucket):
    """Create directory structure for releases"""
    print(f"\n==> Creating directory structure")

    directories = [
        'releases/0.0.1-alpha/',
        'releases/latest/',
    ]

    for directory in directories:
        try:
            # Create empty object to represent directory
            bucket.put_object(directory, '')
            print(f"✓ Created: {directory}")
        except Exception as e:
            print(f"! {directory}: {e}")

def print_summary(accelerated_endpoint):
    """Print configuration summary"""
    print("\n" + "="*60)
    print("OSS Bucket Configuration Summary")
    print("="*60)
    print(f"Bucket Name:    {BUCKET_NAME}")
    print(f"Region:         {REGION}")
    print(f"Endpoint:       {ENDPOINT}")
    print(f"Domain:         https://{DOMAIN}")
    if accelerated_endpoint:
        print(f"Accelerated:    https://{accelerated_endpoint}")
    print("\nGitHub Secrets to add:")
    print(f"  ALIYUN_ACCESS_KEY_ID:     {ACCESS_KEY_ID}")
    print(f"  ALIYUN_ACCESS_KEY_SECRET: {ACCESS_KEY_SECRET}")
    print(f"  ALIYUN_OSS_BUCKET:        {BUCKET_NAME}")
    print(f"  ALIYUN_OSS_ENDPOINT:      {ENDPOINT}")
    print(f"  ALIYUN_OSS_REGION:        {REGION}")
    print("\nUpload URL pattern:")
    print(f"  https://{DOMAIN}/releases/{{version}}/{{filename}}")
    print("="*60 + "\n")

def main():
    print("Aliyun OSS Setup for Manul Package Distribution")
    print("="*60)

    try:
        # Create bucket
        bucket = create_bucket()

        # Enable transfer acceleration
        accelerated_endpoint = enable_transfer_acceleration(bucket)

        # Set CORS policy
        set_cors_policy(bucket)

        # Bind custom domain
        cname_target = bind_domain(bucket)

        # Configure DNS
        if cname_target:
            configure_dns(cname_target)

        # Create directory structure
        create_directory_structure(bucket)

        # Print summary
        print_summary(accelerated_endpoint)

        print("✓ OSS setup complete!")

    except Exception as e:
        print(f"\n✗ Setup failed: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == '__main__':
    main()
