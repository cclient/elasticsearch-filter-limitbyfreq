Limit Token Filter for Elasticsearch
==================================

Filter: limit_by_freq

Parameter: max_token_count(default:512)

Desc: token order by freq desc and limit top 

freq num is stored in 'payload' to be used in future

Install
-------

1.download or compile

* download pre-build package from here: https://github.com/cclient/elasticsearch-filter-limitbyfreq/releases
    
    unzip plugin to folder `your-es-root/plugins/`

2.restart elasticsearch


#### Quick Example

1.create a index

```bash
curl -XPUT http://localhost:9200/test_index -d'
{
	"settings": {
		"analysis": {
            "filter": {
                "my_limit": { 
                    "type":"limit_by_freq",
                    "max_token_count":2
                }
            },
            "analyzer": {
                "limit_test": {
                    "tokenizer": "standard",
                    "filter": [
						"my_limit"
                    ]
                }
            }
        }
	},
	"mappings": {
		"test": {
			"properties": {
				"desc":		{ 
					"type": "text",
					"analyzer": "limit_test"
				}
			}
		}
	}
}'
```

2.test 

```bash
curl -XPOST http://localhost:9200/test_index/_analyze?tokenizer=standard&filter=limit_by_freq -d'
hello hyper log log'
```

Result

```json
{
    "tokens": [
        {
            "token": "log",
            "start_offset": 0,
            "end_offset": 0,
            "type": "TOP_TOKEN",
            "position": 0
        },
        {
            "token": "hello",
            "start_offset": 0,
            "end_offset": 0,
            "type": "TOP_TOKEN",
            "position": 1
        },
        {
            "token": "hyper",
            "start_offset": 0,
            "end_offset": 0,
            "type": "TOP_TOKEN",
            "position": 2
        }
    ]
}
```



```bash
curl -XPOST http://127.0.0.1:9200/test_index/_analyze?analyzer=limit_test -d'
hello hyper log log'
```

Result

```json
{
    "tokens": [
        {
            "token": "log",
            "start_offset": 0,
            "end_offset": 0,
            "type": "TOP_TOKEN",
            "position": 0
        },
        {
            "token": "hello",
            "start_offset": 0,
            "end_offset": 0,
            "type": "TOP_TOKEN",
            "position": 1
        }
    ]
}
```