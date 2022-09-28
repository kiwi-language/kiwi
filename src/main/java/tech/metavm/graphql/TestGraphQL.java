package tech.metavm.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;

import java.util.List;
import java.util.Map;

public class TestGraphQL {


    private static final List<Map<String, Object>> PRODUCTS = List.of(
            Map.of("id", "1", "title", "apple"),
            Map.of("id", "2", "title", "computer"),
            Map.of("id", "3", "title", "dog")
    );

    private static final List<Map<String, Object>> USERS = List.of(
            Map.of("id", "1", "name", "Leen", "products", List.of(PRODUCTS.get(0), PRODUCTS.get(1))),
            Map.of("id", "2", "name", "Lyq", "products", List.of(PRODUCTS.get(2))),
            Map.of("id", "3", "name", "Jjk", "products", List.of())
    );

    public static void main(String[] args) {
        DataFetcher<Map<String, Object>> userFetcher = environment -> {
            String id = environment.getArgument("id");
            return USERS.stream()
                    .filter(user -> id.equals(user.get("id")))
                    .findAny()
                    .orElse(null);
        };

        GraphQLCodeRegistry.Builder codeBuilder = GraphQLCodeRegistry.newCodeRegistry();

        GraphQLObjectType productType = createProductType(codeBuilder);

        GraphQLObjectType userType = GraphQLObjectType.newObject()
                .name("User")
                .field(
                        GraphQLFieldDefinition.newFieldDefinition()
                                .name("id")
                                .type(Scalars.GraphQLID)
                                .build()
                )
                .field(
                        GraphQLFieldDefinition.newFieldDefinition()
                                .name("name")
                                .type(Scalars.GraphQLString)
                                .build()
                )
                .field(
                        GraphQLFieldDefinition.newFieldDefinition()
                                .name("products")
                                .type(GraphQLList.list(productType))
                                .build()
                )
                .build();

            codeBuilder.dataFetcher(
                        FieldCoordinates.coordinates("Query", "user"),
                        userFetcher
                );

        GraphQLObjectType queryType = GraphQLObjectType.newObject()
                .name("Query")
                .field(
                        GraphQLFieldDefinition.newFieldDefinition()
                                .name("user")
                                .argument(
                                        GraphQLArgument.newArgument()
                                                .name("id")
                                                .type(Scalars.GraphQLID)
                                                .build()
                                )
                                .type(userType)
                                .build()
                )
                .build();

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(queryType)
                .codeRegistry(codeBuilder.build())
                .build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query("query { user(objectId: \"2\") { name products { objectId title } } }")
                .build();

        ExecutionResult result = graphQL.execute(executionInput);

        Map<String, Object> user = result.getData();

        System.out.println(user);
    }

    private static GraphQLObjectType createProductType(GraphQLCodeRegistry.Builder codeBuilder) {
        DataFetcher<Map<String, Object>> productFetcher = environment -> {
            String id = environment.getArgument("id");
            return PRODUCTS.stream()
                    .filter(user -> id.equals(user.get("id")))
                    .findAny()
                    .orElse(null);
        };

        GraphQLObjectType productType = GraphQLObjectType.newObject()
                .name("Product")
                .field(
                        GraphQLFieldDefinition.newFieldDefinition()
                                .name("id")
                                .type(Scalars.GraphQLID)
                                .build()
                )
                .field(
                        GraphQLFieldDefinition.newFieldDefinition()
                                .name("title")
                                .type(Scalars.GraphQLString)
                                .build()
                )
                .build();

        codeBuilder.dataFetcher(
                FieldCoordinates.coordinates("Query", "product"),
                productFetcher
        );

        return productType;
    }

}
