package cn.com.dhc.es;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.CreateOperation;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

public class ESClient {

    private static ElasticsearchClient client;
    private static ElasticsearchAsyncClient asyncClient;
    private static ElasticsearchTransport transport;
    private static final String INDEX_LNNT = "lnnt";

    public static void main(String[] args) throws Exception {
        // 初始化ES服务器的连接
        initESConnection();

        // 操作索引
//        operationIndexLambda();

        // 操作文档
//        operationDocumentLambda();

        // 查询文档
//        queryDocumentLambda();

        // 异步操作
        asyncClientOperation();
    }
    private static void asyncClientOperation() throws Exception {
        asyncClient.indices().create(
                req -> req.index("newindex")
        ).thenApply(
            resp -> resp.acknowledged()
        ).whenComplete(
            (resp, error) -> {
                System.out.println("回调方法");
                if (resp) {
                    System.out.println(resp);
                } else {
                    error.printStackTrace();
                }
            }
        );
        System.out.println("主线程代码...");
    }
    private static void queryDocumentLambda() throws Exception {
        System.out.println(client.search(
            req ->
            {
                req.query(
                    q -> q.match(
                        m -> m.field("name").query("张三")
                    )
                );
                return req;
            },
            Object.class
        ).hits());

        transport.close();
    }
    private static void queryDocument() throws Exception {
        MatchQuery matchQuery = new MatchQuery.Builder()
                .field("age").query(30)
                .build();
        Query query = new Query.Builder()
                .match(matchQuery)
                .build();
        SearchRequest searchRequest = new SearchRequest.Builder()
            .query(query)
            .build();
        SearchResponse<Object> search = client.search(searchRequest, Object.class);
        System.out.println(search);

        transport.close();
    }
    private static void operationDocumentLambda() throws Exception {
        User user = new User();
        user.setId(1001);
        user.setName("张三");
        user.setAge(30);

        System.out.println(client.create(
            req ->
                req
                    .index(INDEX_LNNT)
                    .id("1001")
                    .document(new User(1001, "zhangsan", 30))
        ).result());

        List<User> users = new ArrayList<User>();
        for (int i = 0; i < 5; i++) {
            users.add(new User(3000 + i, "lisi" + i, 40 + i));
        }
        //批量添加数据
        client.bulk(
            req -> {
                users.forEach(
                    u -> {
                        req.operations(
                            b -> b.create(
                                d ->
                                    d.index(INDEX_LNNT).id(u.getId().toString()).document(u)
                            )
                        );
                    }
                );
                return req;
            }
        );

        DeleteResponse deleteResponse = client.delete(req -> req.index(INDEX_LNNT).id("3001"));
        System.out.println(deleteResponse);
    }
    private static void operationDocument() throws Exception {
        User user = new User();
        user.setId(1001);
        user.setName("张三");
        user.setAge(30);

        CreateRequest createRequest = new CreateRequest.Builder<User>()
                .index(INDEX_LNNT)
                .id("1001")
                .document(user)
                .build();

        // 创建文档
//        CreateResponse createResponse = client.create(createRequest);
//        System.out.println("文档创建的响应对象: " + createResponse);

        // 批量添加数据
        List<BulkOperation> opts = new ArrayList<BulkOperation>();
        for (int i = 1; i < 5; i ++) {
            CreateOperation<User> optObj = new CreateOperation.Builder<User>()
                    .index(INDEX_LNNT)
                    .id("200" )
                    .document(new User(200 + i, "张三" + i, + 30 + i))
                    .build();
            BulkOperation opt = new BulkOperation.Builder().create(optObj).build();
            opts.add(opt);
        }

        BulkRequest bulkRequest = new BulkRequest.Builder().
                operations(opts)
                .build();
//        BulkResponse bulkResponse = client.bulk(bulkRequest);
//        System.out.println("批量新增响应数据" + bulkResponse);

        // 文档的删除
        DeleteRequest deleteRequest = new DeleteRequest.Builder()
                .index(INDEX_LNNT)
                .id("2001")
                .build();
        DeleteResponse deleteResponse = client.delete(deleteRequest);

        System.out.println(deleteResponse);

        transport.close();
    }
    private static void operationIndexLambda() throws Exception {
        ElasticsearchIndicesClient indices = client.indices();
        boolean fag = indices.exists(req -> req.index(INDEX_LNNT)).value();
        if (fag) {
            System.out.println("索引" + INDEX_LNNT + "已经存在");
        } else {
            CreateIndexResponse createIndexResponse = indices.create(req -> req.index(INDEX_LNNT));
            System.out.println("创建索引的响应对象 = " + createIndexResponse);
        }
        GetIndexResponse getIndexResponse = indices.get(req -> req.index(INDEX_LNNT));
        System.out.println("查询的响应结果: " + getIndexResponse);

        DeleteIndexResponse deleteIndexResponse = indices.delete(req -> req.index(INDEX_LNNT));
        System.out.println("删除索引成功" + deleteIndexResponse.acknowledged());

        transport.close();
    }
    private static void operationIndex() throws Exception {

        // 获取索引客户端对象
        ElasticsearchIndicesClient indices = client.indices();

        // 判断索引是否存在
        ExistsRequest existsRequest = new ExistsRequest.Builder().index(INDEX_LNNT).build();
        boolean flg = indices.exists(existsRequest).value();
        if (flg) {
            System.out.println("索引" + INDEX_LNNT + "已经存在");
        } else {
            // 创建索引
            // 需要采用构建器方式来构建对象, ESAPI的对象基本上都是采用这种方式
            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(INDEX_LNNT).build();
            CreateIndexResponse createIndexResponse = indices.create(createIndexRequest);
            System.out.println("创建索引的响应对象 = " + createIndexResponse);
        }

        // 查询索引
        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder().index(INDEX_LNNT).build();
        GetIndexResponse getIndexResponse = indices.get(getIndexRequest);
//        IndexState lnnt = getIndexResponse.get("lnnt");
        System.out.println("查询的响应结果: " + getIndexResponse);

        // 删除索引
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest.Builder().index(INDEX_LNNT).build();
        DeleteIndexResponse deleteIndexResponse = indices.delete(deleteIndexRequest);
        System.out.println("删除索引成功" + deleteIndexResponse.acknowledged());

        transport.close();
    }
    private static void initESConnection() throws Exception {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "SjnYhhhBBvxLcMJ_7B66"));
        Path caCertificatePath = Paths.get("certs/http_ca.crt");
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate trustedCa;
        try (InputStream is = Files.newInputStream(caCertificatePath)) {
            trustedCa = factory.generateCertificate(is);
        }
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);
        SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null);
        final SSLContext sslContext = sslContextBuilder.build();

        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setSSLContext(sslContext)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        RestClient restClient = builder.build();

        transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // 同步客户端对象
        client = new ElasticsearchClient(transport);
        //异步客户端对象
        asyncClient = new ElasticsearchAsyncClient(transport);
//        transport.close();
    }
}
