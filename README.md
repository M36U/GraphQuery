# GraphQuery

A GraphQL query generator for Java projects.

## Example

### Java code

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
                allUsers.withField("avatar").includeIf("true");
                allUsers.withField("email").skipIf("true");
            }).withAlias("users");
        });
    });
    
### Generated query

##### Query

    query ($true: Boolean!) {
      users: allUsers(count: 1) {
        id
        first: firstName
        last: lastName
        avatar @include(if: $true)
        email @skip(if: $true)
      }
    }
    
##### Variables

    {
      "true": true
    }
    
### Example response

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