# 创建索引

### PUT 索引名称(小写)

```PUT test_index```

### PUT 索引
```json
// 增加配置: JSON格式的主体内容
PUT test_index_1
{
  "aliases": {
    "test1": {}
  }
}
```

# 删除索引

### DELETE 索引名称

```DELETE test_index_1```

# 修改索引配置

### ES软件不允许修改索引信息
```json
POST test_index_1
{
  "aliases": {
    "test1": {}
  }
}
```

# HEAD索引 HTTP状态码: 200, 404

```HEAD test_index```

# 查询索引

### GET 索引名称

```GET test_index```

```GET test_index_1```

```GET test1```

### 查询所有索引

```GET _cat/indices```

# 创建文档(索引数据) - 增加唯一性标识(手动, 自动)

```PUT test_doc```

```json
PUT test_doc/_doc/1001
{
  "id": 1001,
  "name": "张三",
  "age": 30
}
```
```json
POST test_doc/_doc
{
  "id": 1002,
  "name": "李四",
  "age": 40
}
```

# 查询文档

```GET test_doc/_doc/1001```

# 查询索引中所有的文档数据

```GET test_doc/_search```

# 修改数据
```json
PUT test_doc/_doc/1001
{
  "id": 10011,
  "name": "张三1",
  "age": 300,
  "tel": 123123123
}

POST test_doc/_doc/yYhVLIMBI9-OlF_xs-Ir
{
  "id": 10012,
  "name": "张三2",
  "age": 200,
  "tel": 123123123
}
```

# 删除数据

```DELETE test_doc/_doc/yYhVLIMBI9-OlF_xs-Ir```

# 创建索引并插入数据

```PUT test_query```

```PUT test_query/_bulk```
```json
{"index": {"_index": "test_query", "_id": "1001"}}
{"id": "1001", "name": "zhang san", "age": 30}
{"index": {"_index": "test_query", "_id": "1002"}}
{"id": "1002", "name": "li si", "age": 40}
{"index": {"_index": "test_query", "_id": "1003"}}
{"id": "1003", "name": "wang wu", "age": 50}
{"index": {"_index": "test_query", "_id": "1004"}}
{"id": "1004", "name": "zhangsan", "age": 30}
{"index": {"_index": "test_query", "_id": "1005"}}
{"id": "1005", "name": "lisi", "age": 40}
{"index": {"_index": "test_query", "_id": "1006"}}
{"id": "1006", "name": "wangwu", "age": 50}
```

```GET test_query/_search```

```json
{
  // 条件
  "query": {
    // 匹配所有条件
    //"match_all": {},
    
    // match是分词查询, ES会将数据分词(关键词)保存
    "match": {
      "name": "zhang"
    }
  }
}
```


# 对查询结果的字段进行限制
```json
GET test_query/_search
{
  "_source": ["name", "age"], 
  "query": {
    // 完整关键词匹配
    "term": {
      "name": {
        "value": "zhangsan"
      }
    }
  }
}
```

# 组合多个条件 or
```json
GET test_query/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "name": "zhang"
          }
        },{
          "match": {
            "age": 40
          }
        }
      ]
    }
  }
}
```

# 排序后查询
```json
GET test_query/_search
{
  "query": {
    "match": {
      "name": "zhang li"
    }
  },
  "sort": [
    {
    "age": {
      "order": "desc"
    }
    }
  ]
}
```

# 分页查询
```json
GET test_query/_search
{
  "query": {
    "match_all": {}
  },
  // from = (pageno - 1) * size
  "from": 2,
  "size": 2
}
```

# 分组查询
```json
GET test_query/_search
{
  // 聚合规则
  "aggs": {
    "ageGroup": {
      "terms": {
        "field": "age"
      }
    }
  },
  // 去掉原数据,
  "size": 0
}
```

# 分组后聚合(求和)
```json
GET test_query/_search
{
  "aggs": {
    "ageGroup": {
      "terms": {
        "field": "age"
      },
      "aggs": {
        "ageSum": {
          "sum": {
            "field": "age"
          }
        }
      }      
    }
  },
  "size": 0
}
```

# 求平均值
```json
GET test_query/_search
{
  "aggs": {
    "avgAge": {
      "avg": {
        "field": "age"
      }
    }
  },
  "size": 0
}
```

# 获取前几名操作
```json
GET test_query/_search
{
  "aggs": {
    "top3": {
      "top_hits": {
        "sort": [
          {
            "age": {
              "order": "desc"
            }
          }
        ], 
        "size": 3
      }
    }
  },
  "size": 0
}
```

# 创建模板(可以多次操作)
```json
PUT _template/mytemplate
{
  "index_patterns": [
    "my*"  
  ],
  "settings": {
    "index": {
      "number_of_shards": 2
    }
  },
  "mappings": {
    "properties": {
      "now": {
        "type": "date",
        "format": "yyyy/MM/dd"
      }
    }
  }
}
```

# 查看模板
```GET _template/mytemplate```

# 使用模板创建并查看索引
```PUT my_test_temp```
```GET my_test_temp```

# 删除模板
```DELETE _template/mytemplate```

# 文档评分机制

```PUT test_score```

```json
get test_score/_search?explain=true
{
  "query": {
    "match": {
      "text": "zhang"
    }
  }
}
```
```json
PUT test_score/_doc/1001
{
  "text": "zhang kai shuang bi, yong bao wei lai"
}
```

```json
PUT test_score/_doc/1002
{
  "text": "zhang san"
}
```

### TF(词频)

  Term Frequency: 搜索文本中的各个词条(term)在查询文本中出现了多少次, 出现次数越多, 就越相关, 得分会比较高

### IDF(逆文档频率)

  Inverse Document Frequency: 搜素文本中的各个词条(term)在整个索引的所有文档中出现了多少次, 出现的次数越多, 说明越不重要, 也就越不相关, 得分就比较低

### TF && IDF

  ```PUT lnnt```

  ```json
  PUT lnnt/_doc/1001
  {
    "text": "java"
  }
  ```

  计算公式: ```boost * idf * tf```
  ```tf=freq / (freq + k1 * (1 - b + b * dl / avgdl))```
  ```tf = 1 / (1 + 1.2 * (1 - 0.75 + 0.75 * 1 / 1))```
  freq: occurrences of term within document
  k1: term saturation parameter
  b: length normalization parameter
  dl: length of field
  avgdl: average length of field (field / documents)

  ```idf=log(1 + (N - n + 0.5) / (n + 0.5))```
  ```idf=log(1 + (2 - 2 + 0.5) / (2 + 0.5))```
  N: total number of documents with field
  n: number of documents containing term

  ```json
  GET lnnt/_search?explain=true
  {
    "query": {
      "match": {
        "text": "java"
      }
    }
  }
  ```

  ```json
  PUT lnnt/_doc/1002
  {
    "text": "java bigdata"
  }
  ```

  计算公式: ```boost * idf * tf```
  ```tf=freq / (freq + k1 * (1 - b + b * dl / avgdl))```

  <strong>doc1</strong>
    ```tf = 1 / (1 + 1.2 * (1 - 0.75 + 0.75 * 1 / 1.5))```
    avgdl: average length of field (field(3) / documents(2))

    ```idf=log(1 + (N - n + 0.5) / (n + 0.5))```
    ```idf=log(1 + (2 - 2 + 0.5) / (2 + 0.5))``

  <strong>doc2</strong>
    ```tf = 1 / (1 + 1.2 * (1 - 0.75 + 0.75 * 2 / 1.5))```
    avgdl: average length of field (field(3) / documents(2))

    ```idf=log(1 + (N - n + 0.5) / (n + 0.5))```
    ```idf=log(1 + (2 - 2 + 0.5) / (2 + 0.5))``

  ```json
  GET lnnt/_search?explain=true
  {
    "query": {
      "match": {
        "text": "java"
      }
    }
  }
  ```

### 权重分析

```PUT lnnt```

```json
PUT lnnt/_doc/1001
{
  "title": "Hadoop is a Framework",
  "content": "Hadoop 是一个大数据框架"
}

PUT lnnt/_doc/1002
{
  "title": "Hive is a SQL Tools",
  "content": "Hive 是一个SQL工具"
}

PUT lnnt/_doc/1003
{
  "title": "Spark is a Framework",
  "content": "Spark 是一个分布式引擎"
}
```

> 权重 = 2.2 * 查询权重
```json
GET lnnt/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "title": {"query": "Hadoop", "boost": 1}
          }
        },
        {
          "match": {
            "title": {"query": "Hive", "boost": 1}
          }
        },
        {
          "match": {
            "title": {"query": "Spark", "boost": 2}
          }
        }
      ]
    }
  }
}
```