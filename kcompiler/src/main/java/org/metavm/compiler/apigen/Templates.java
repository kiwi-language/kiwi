package org.metavm.compiler.apigen;

public class Templates {

    public static final String COMMON_DATA_STRUCTURES = """
            export interface SearchResult<T> {
                items: T[]
                total: number
            }
            
            export interface ErrorResponse {
              code: number;
              message: string;
            }
            
            export class ApiError extends Error {
              response: Response;
              body: ErrorResponse;
                        
              constructor(response: Response, body: ErrorResponse) {
                super(body.message);
                this.name = 'HttpError';
                this.response = response;
                this.body = body;
              }
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
                    const errorBody: ErrorResponse = await response.json();
                    throw new ApiError(response, errorBody);
                }
                        
                if (response.status === 204) {
                    return undefined as T;
                }
                        
                const contentType = response.headers.get('content-type');
                        
                if (contentType && contentType.includes('application/json')) {
                    return await response.json() as T;
                }
               
                return await response.text() as T;
            }
            """;

}
