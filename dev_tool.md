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