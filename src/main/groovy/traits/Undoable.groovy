package traits

import java.lang.reflect.Method

trait Undoable {

    Stack<List<Tuple2<Method, Object>>> stack = new Stack<>()
    List<Tuple2<Method, Object>> tuples
    private boolean freeze
    private boolean inProgress
    private boolean useBlocks = true

    void useBlocks(boolean useBlocks) {
        freeze = true
        if (useBlocks) {
            this.inProgress = false
        } else {
            this.inProgress = true
            this.tuples = null
        }
        this.useBlocks = useBlocks
        freeze = false
    }

    void undo() {
        freeze = true
        if (!stack.empty()) {
            List<Tuple2<Method, Object>> tuples = stack.pop()
            for (Tuple2<Method, Object> tuple : tuples) {
                Object arg = tuple.getSecond()
                if (arg == null) {
                    tuple.getFirst().invoke(this, [null].toArray())
                } else {
                    tuple.getFirst().invoke(this, arg)
                }
            }
        }
        freeze = false
    }

    boolean isFrozen() {
        return this.freeze
    }

    boolean isInProgress() {
        return this.inProgress
    }

    boolean isUsingBlocks() {
        return this.useBlocks
    }

    void start() {
        freeze = true
        if (!useBlocks) {
            freeze = false
            throw new IllegalStateException("blocking is currently disabled - to enable, call useBlocks(true)")
        }
        if (inProgress) {
            freeze = false
            throw new IllegalStateException("block is already in progress - you must first call finish() before calling start()")
        }
        inProgress = true
        tuples = []
        freeze = false
    }

    void finish() {
        freeze = true
        if (!useBlocks) {
            freeze = false
            throw new IllegalStateException("blocking is currently disabled - to enable, call useBlocks(true)")
        }
        if (!inProgress) {
            freeze = false
            throw new IllegalStateException("block is not in progress - you must first call start()")
        }
        inProgress = false
        stack.push(tuples)
        freeze = false
    }

}