# GraphQuery

A GraphQL query generator for Java projects.

## Example

### Java code

```java
public class QueryTest {
    public static void main(String... args) throws Exception {
        GraphQLQuery query = new GraphQLQuery($ -> {
            try {
                $.withUrl("https://fakerql.com/graphql");
            } catch (Exception e) {
                e.printStackTrace();
            }
        
            $.withArgument("someBool", "Boolean!");
        
            $.withVariable("someBool", true);
        
            $.withObject(root -> {
                root.withObject("allUsers", allUsers -> {
                    allUsers.withArgument("count", 1);
                    allUsers.withField("id");
                    allUsers.withField("firstName").withAlias("first");
                    allUsers.withField("lastName").withAlias("last");
                    allUsers.withField("avatar").includeIf("someBool");
                    allUsers.withField("email").skipIf("someBool");
                }).withAlias("users");
            });
        });
        
        FutureTask<String> task = query.createRequest();
        task.run();
        String jsonResponse = task.get();
    }
}
```

### Generated query

##### Query
```graphql
query ($someBool: Boolean!) {
  users: allUsers(count: 1) {
    id
    first: firstName
    last: lastName
    avatar @include(if: $someBool)
    email @skip(if: $someBool)
  }
}
```
    
##### Variables
```json
{
  "someBool": true
}
```
 
### Example response

```json
{
  "data": {
    "users": [
      {
        "id": "cjkaco8zd000p2c10u73gx48k",
        "first": "Jamison",
        "last": "Parisian",
        "avatar": "https://s3.amazonaws.com/uifaces/faces/twitter/gabrielizalo/128.jpg"
      }
    ]
  }
}
```