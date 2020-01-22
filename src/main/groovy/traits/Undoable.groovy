package traits

import java.lang.reflect.Method

trait Undoable {

    private Stack<List<Tuple2<Method, Object>>> stack = new Stack<>()
    List<Tuple2<Method, Object>> tuples
    private boolean freeze
    private boolean inProgress

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

    void start() {
        if (inProgress) {
            throw new IllegalStateException("block is already in progress - you must first call finish() before calling start()")
        }
        freeze = true
        inProgress = true
        tuples = []
        freeze = false
    }

    void finish() {
        if (!inProgress) {
            throw new IllegalStateException("block is not in progress - you must first call start()")
        }
        freeze = true
        inProgress = false
        stack.push(tuples)
        freeze = false
    }

}