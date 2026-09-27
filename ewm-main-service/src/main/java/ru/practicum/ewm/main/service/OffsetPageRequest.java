package ru.practicum.ewm.main.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public final class OffsetPageRequest implements Pageable {
    private final long offset;
    private final int size;
    private final Sort sort;

    public OffsetPageRequest(long offset, int size, Sort sort) {
        if (offset < 0 || size < 1) throw new IllegalArgumentException("Invalid pagination values");
        this.offset = offset;
        this.size = size;
        this.sort = sort;
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / size);
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + size, size, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetPageRequest(Math.max(offset - size, 0), size, sort) : first();
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, size, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) throw new IllegalArgumentException("Page index must not be less than zero");
        return new OffsetPageRequest((long) pageNumber * size, size, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }

    @Override
    public Optional<Pageable> toOptional() {
        return Optional.of(this);
    }
}
