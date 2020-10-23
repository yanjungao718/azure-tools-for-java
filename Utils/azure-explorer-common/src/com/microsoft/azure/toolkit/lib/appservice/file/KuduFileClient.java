package com.microsoft.azure.toolkit.lib.appservice.file;

import okhttp3.ResponseBody;
import retrofit2.http.*;
import rx.Observable;

import java.util.List;

public interface KuduFileClient extends AppServiceFileClient {
    @Headers({
                 "Content-Type: application/json; charset=utf-8",
                 "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps getFile"
             })
    @GET("api/vfs/{path}")
    Observable<ResponseBody> getFile(@Path("path") String path);

    @Headers({
                 "Content-Type: application/json; charset=utf-8",
                 "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps getFilesInDirectory"
             })
    @GET("api/vfs/{path}/")
    Observable<List<KuduFile>> getFilesInDirectory(@Path("path") String path);

    @Headers({
                 "Content-Type: application/json; charset=utf-8",
                 "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps saveFile"
             })
    @PUT("api/vfs/{path}")
    Observable<ResponseBody> saveFile(@Path("path") String path);

    @Headers({
                 "Content-Type: application/json; charset=utf-8",
                 "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps createDirectory"
             })
    @PUT("api/vfs/{path}/")
    Observable<ResponseBody> createDirectory(@Path("path") String path);

    @Headers({
                 "Content-Type: application/json; charset=utf-8",
                 "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps deleteFile"
             })
    @DELETE("api/vfs/{path}")
    Observable<ResponseBody> deleteFile(@Path("path") String path);
}
