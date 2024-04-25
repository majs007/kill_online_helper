package kill.online.helper.client


import kill.online.helper.client.route.AppClient
import kill.online.helper.client.route.ZeroTierClient
import kill.online.helper.utils.NetworkUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

/**
 * 通过网络IO读写数据
 */
object NetworkRepository {
    //创建拦截器
    private val zeroInterceptor = Interceptor { chain ->
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val url = request.url()
        val builder = url.newBuilder()
        requestBuilder.url(builder.build())
            .method(request.method(), request.body())
            .addHeader("clientType", "android")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", token)
        chain.proceed(requestBuilder.build())
    }
    private val appInterceptor = Interceptor { chain ->
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val url = request.url()
        val builder = url.newBuilder()
        requestBuilder.url(builder.build())
            .method(request.method(), request.body())
            .addHeader("clientType", "android")
            .addHeader("Content-Type", "application/json")
        chain.proceed(requestBuilder.build())
    }

    //创建OKhttp
    private val zeroHttpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        .addInterceptor(zeroInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        // 添加信任所有证书的 SSL Socket Factory
        .sslSocketFactory(
            NetworkUtils.sSLSocketFactory,
            NetworkUtils.trustManager[0] as X509TrustManager
        )
        // 忽略 Hostname 验证
        .hostnameVerifier { _, _ -> true }

    private val appHttpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        .addInterceptor(appInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)


    private val zeroRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.zerotier.com/api/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(zeroHttpClient.build())
        .build()

    private val appRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://127.0.0.1/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(appHttpClient.build())
        .build()

    val ztClient: ZeroTierClient = zeroRetrofit.create(ZeroTierClient::class.java)
    val appClient: AppClient = appRetrofit.create(AppClient::class.java)

}


