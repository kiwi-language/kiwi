package org.metavm.compiler.apigen;

public class Templates {

    public static final String COMMON_DATA_STRUCTURES = """
            export interface Result<T> {
                data: T
                // 0 for success, non-zero for failure
                code: number
                message?: string
            }
            
            export interface SearchResult<T> {
                items: T[]
                total: number
            }
            """;

    public static final String CALL_API = """
            const API_BASE_URL = '';
            
            async function callApi<T>(endpoint: string, method: string, body?: any): Promise<T> {
                console.log(`Calling endpoint: ${endpoint}`)
                const headers: HeadersInit = {'X-App-ID': APP_ID + ''}
                        
                if (body !== undefined) {
                    headers['Content-Type'] = 'application/json';
                }
                        
                console.log("headers: ", headers)
                        
                const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                    method,
                    headers,
                    body: body !== undefined ? JSON.stringify(body) : undefined
                });
                        
                if (!response.ok) {
                    throw new Error(`API call failed: ${response.status} ${response.statusText}`);
                }
                        
                const result: Result<T> = await response.json();
                        
                if (result.code !== 0) {
                    throw new Error(`API error: ${result.message || 'Unknown error'}`);
                }
                        
                return result.data;
            }
            """;

}
