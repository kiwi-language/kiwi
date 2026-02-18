package org.manul.compiler.apigen;

public class Templates {

    public static final String COMMON_DATA_STRUCTURES = """
            export interface SearchResult<T> {
                items: T[]
                total: number
            }
            
            export interface ErrorResponse {
              message: string;
            }
 
            export interface UploadResult {
                url: string
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
        let auth: string | undefined

        let token: string | undefined;

        export function setToken(newToken: string | undefined) {
            token = newToken;
        }

        async function callApi<T>(endpoint: string, method: string, body?: any): Promise<T> {
            const startTime = new Date();
            // Declare response and error variables outside the try block to access them in 'finally'
            let response: Response | undefined;
            let errorOccurred: any;
            // NEW: Variable to hold the response body for logging
            let responseBodyForLogging: any;
            // Capture original body for logging before potential modification
            const requestBodyForLogging = body;

            try {
                const headers: HeadersInit = {'X-Refresh-Policy': 'WAIT', 'X-Return-Full-Object': RETURN_FULL_OBJECT + '' };

                if (token) {
                    headers['Authorization'] = `Bearer ${token}`;
                }

                let url = `${API_BASE_URL}${endpoint}`;

                if (method === 'GET' && body !== undefined) {
                    const params = new URLSearchParams();
                    Object.keys(body).forEach(key => {
                        const value = body[key];
                        if (value !== undefined && value !== null && value != '') {
                            if (Array.isArray(value)) {
                                value.forEach((item: any) => params.append(key, item.toString()));
                            } else {
                                params.append(key, value.toString());
                            }
                        }
                    });
                    const queryString = params.toString();
                    if (queryString) {
                        url += (url.includes('?') ? '&' : '?') + queryString;
                    }
                    body = undefined;
                } else if (body !== undefined) {
                    headers['Content-Type'] = 'application/json';
                }

                response = await fetch(url, {
                    method,
                    headers,
                    body: body !== undefined ? JSON.stringify(body) : undefined
                });

                if (!response.ok) {
                    const errorBody: ErrorResponse = await response.json();
                    // NEW: Capture the error body for logging
                    responseBodyForLogging = errorBody;
                    throw new ApiError(response, errorBody);
                }

                if (response.status === 204) {
                    return undefined as T;
                }

                const contentType = response.headers.get('content-type');

                if (contentType && contentType.includes('application/json')) {
                    // NEW: Capture the JSON body before returning
                    const data = await response.json();
                    responseBodyForLogging = data;
                    return data as T;
                }

                // NEW: Capture the text body before returning
                const textData = await response.text();
                responseBodyForLogging = textData;
                return textData as T;

            } catch (error) {
                errorOccurred = error;
                // Re-throw the error so the calling function can handle it
                throw error;
            } finally {
                const endTime = new Date();
                const duration = endTime.getTime() - startTime.getTime();
                const status = response ? response.status : 'FETCH_FAILED';
                const outcome = errorOccurred ? `Error: ${errorOccurred.message}` : `Status: ${status}`;

                console.log(
                    `API Call: ${method} ${endpoint} | ` +
                    `Outcome: ${outcome} | ` +
                    `Duration: ${duration}ms | ` +
                    `Timestamps: ${startTime.toISOString()} -> ${endTime.toISOString()}`
                );

                // NEW: Log the request and response bodies
                if (requestBodyForLogging !== undefined) {
                    console.log('Request Body:', requestBodyForLogging);
                }
                if (responseBodyForLogging !== undefined) {
                    console.log('Response Body:', responseBodyForLogging);
                }
            }
        }
        """;

    public static final String UPLOAD_API = """
            export const systemApi = {
            
                upload: async (file: File): Promise<UploadResult> => {
                    let formData = new FormData()
                    formData.append('file', file)
                    const response = await fetch('/files/v2', {
                        method: 'POST',
                        headers: {
                            'X-App-ID': APP_ID + '',
                        },
                        body: formData,
                    });
        
                    if (!response.ok) {
                        const errorBody: ErrorResponse = await response.json();
                        throw new ApiError(response, errorBody);
                    }
                    
                    return await response.json() as UploadResult;
                }
                
            }""";

    public static final String EXAMPLE_MNL = """
            class Book(
                var title: string,
                var author: string
            )
            """;

}
