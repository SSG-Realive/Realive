package com.realive.domain.customer;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCustomer is a Querydsl query type for Customer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomer extends EntityPathBase<Customer> {

    private static final long serialVersionUID = -795338459L;

    public static final QCustomer customer = new QCustomer("customer");

    public final StringPath address = createString("address");

    public final DatePath<java.time.LocalDate> birth = createDate("birth", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> created = createDateTime("created", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final EnumPath<Gender> gender = createEnum("gender", Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isVerified = createBoolean("isVerified");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final NumberPath<Integer> penaltyScore = createNumber("penaltyScore", Integer.class);

    public final StringPath phone = createString("phone");

    public final EnumPath<SignupMethod> signupMethod = createEnum("signupMethod", SignupMethod.class);

    public final DateTimePath<java.time.LocalDateTime> updated = createDateTime("updated", java.time.LocalDateTime.class);

    public QCustomer(String variable) {
        super(Customer.class, forVariable(variable));
    }

    public QCustomer(Path<? extends Customer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCustomer(PathMetadata metadata) {
        super(Customer.class, metadata);
    }

}

