/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.linq.provider.collection;

import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import groovy.transform.Internal;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents the queryable collections
 *
 * @param <T> the type of Queryable element
 * @since 4.0.0
 */
@Internal
public class QueryableCollection<T> implements Queryable<T>, Iterable<T> {
    private final Iterable<T> sourceIterable;
    private Stream<T> sourceStream;

    public static <T> Queryable<T> from(Iterable<T> sourceIterable) {
        return new QueryableCollection<>(sourceIterable);
    }

    @SuppressWarnings("unchecked")
    public static <T> Queryable<T> from(Stream<? extends T> sourceStream) {
        Iterable<T> sourceIterable = (Iterable<T>) toIterable(sourceStream);
        return from(sourceIterable);
    }

    public static <T> Queryable<T> from(Queryable<T> queryable) {
        return queryable;
    }

    private QueryableCollection(Iterable<T> sourceIterable) {
        this.sourceIterable = sourceIterable;
        this.sourceStream = toStream(sourceIterable);
    }

    @Override
    public Iterator<T> iterator() {
        return sourceIterable.iterator();
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> innerJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p ->
                                queryable.stream()
                                        .filter(c -> joiner.test(p, c))
                                        .map(c -> Tuple.tuple(p, c)));

        return from(stream);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> leftJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return outerJoin(this, queryable, joiner);
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> rightJoin(Queryable<? extends U> queryable, BiPredicate<? super T, ? super U> joiner) {
        return outerJoin(queryable, this, (a, b) -> joiner.test(b, a)).select(e -> Tuple.tuple(e.getV2(), e.getV1()));
    }

    @Override
    public <U> Queryable<Tuple2<T, U>> crossJoin(Queryable<? extends U> queryable) {
        Stream<Tuple2<T, U>> stream =
                this.stream()
                        .flatMap(p ->
                                queryable.stream()
                                        .map(c -> Tuple.tuple(p, c)));

        return from(stream);
    }

    @Override
    public Queryable<T> where(Predicate<? super T> filter) {
        Stream<T> stream = this.stream().filter(filter::test);

        return from(stream);
    }

    @Override
    public <K> Queryable<Tuple2<K, Queryable<T>>> groupBy(Function<? super T, ? extends K> classifier, BiPredicate<? super K, ? super Queryable<? extends T>> having) {
        Stream<Tuple2<K, Queryable<T>>> stream =
                this.stream()
                        .collect(Collectors.groupingBy(classifier, Collectors.toList()))
                        .entrySet().stream()
                        .filter(m -> having.test(m.getKey(), from(m.getValue())))
                        .map(m -> Tuple.tuple(m.getKey(), from(m.getValue())));

        return from(stream);
    }

    @Override
    public <U extends Comparable<? super U>> Queryable<T> orderBy(Order<? super T, ? extends U>... orders) {
        Comparator<T> comparator = null;
        for (int i = 0, n = orders.length; i < n; i++) {
            Order<? super T, ? extends U> order = orders[i];
            Comparator<U> ascOrDesc = order.isAsc() ? Comparator.naturalOrder() : Comparator.reverseOrder();
            comparator =
                    0 == i
                            ? Comparator.comparing(order.getKeyExtractor(), ascOrDesc)
                            : comparator.thenComparing(order.getKeyExtractor(), ascOrDesc);
        }

        if (null == comparator) {
            return this;
        }

        return from(this.stream().sorted(comparator));
    }

    @Override
    public Queryable<T> limit(int offset, int size) {
        Stream<T> stream = this.stream().skip(offset).limit(size);

        return from(stream);
    }

    @Override
    public <U> Queryable<U> select(Function<? super T, ? extends U> mapper) {
        Stream<U> stream = this.stream().map(mapper);

        return from(stream);
    }

    @Override
    public Queryable<T> distinct() {
        Stream<? extends T> stream = this.stream().distinct();

        return from(stream);
    }

    @Override
    public Queryable<T> unionAll(Queryable<? extends T> queryable) {
        Stream<T> stream = Stream.concat(this.stream(), queryable.stream());

        return from(stream);
    }

    @Override
    public Queryable<T> intersect(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> queryable.stream().anyMatch(b -> b.equals(a))).distinct();

        return from(stream);
    }

    @Override
    public Queryable<T> minus(Queryable<? extends T> queryable) {
        Stream<T> stream = this.stream().filter(a -> queryable.stream().noneMatch(b -> b.equals(a))).distinct();

        return from(stream);
    }

    @Override
    public List<T> toList() {
        return stream().collect(Collectors.toList());
    }

    @Override
    public Stream<T> stream() {
        try {
            sourceStream = sourceStream.peek(e -> {}); // check whether the stream is usable
        } catch (IllegalStateException ex) {
            sourceStream = toStream(sourceIterable);  // we have to create new stream every time because Java stream can not be reused
        }

        return sourceStream;
    }

    @Override
    public int count() {
        return toList().size();
    }

    @Override
    public BigDecimal sum(Function<? super T, BigDecimal> mapper) {
        return this.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static <T, U> Queryable<Tuple2<T, U>> outerJoin(Queryable<? extends T> queryable1, Queryable<? extends U> queryable2, BiPredicate<? super T, ? super U> joiner) {
        Stream<Tuple2<T, U>> stream =
                queryable1.stream()
                        .flatMap(p ->
                                queryable2.stream()
                                        .map(c -> joiner.test(p, c) ? c : null)
                                        .reduce(new LinkedList<U>(), (r, e) -> {
                                            int size = r.size();
                                            if (0 == size) {
                                                r.add(e);
                                                return r;
                                            }

                                            int lastIndex = size - 1;
                                            Object lastElement = r.get(lastIndex);

                                            if (null != e) {
                                                if (null == lastElement) {
                                                    r.set(lastIndex, e);
                                                } else {
                                                    r.add(e);
                                                }
                                            }

                                            return r;
                                        }, (i, o) -> o).stream()
                                        .map(c -> null == c ? Tuple.tuple(p, null) : Tuple.tuple(p, c)));

        return from(stream);
    }

    private static <T> Stream<T> toStream(Iterable<T> sourceIterable) {
        return StreamSupport.stream(sourceIterable.spliterator(), false);
    }

    private static <T> Iterable<T> toIterable(Stream<T> sourceStream) {
        return sourceStream::iterator;
    }

    @Override
    public String toString() {
        return toList().toString();
    }
}
