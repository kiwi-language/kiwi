branch_name="$(git symbolic-ref HEAD 2>/dev/null)" ||
branch_name="(unnamed branch)"     # detached HEAD
branch_name=${branch_name##refs/heads/}
git checkout dev
git merge $branch_name
git push origin
git checkout $branch_name
ssh root@metavm.tech 'source /root/.profile && bash /root/utils/deploy_app'