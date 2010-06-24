begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * Manages the read/write consistency within memstore. This provides  * an interface for readers to determine what entries to ignore, and  * a mechanism for writers to obtain new write numbers, then "commit"  * the new writes for readers to read (thus forming atomic transactions).  */
end_comment

begin_class
specifier|public
class|class
name|ReadWriteConsistencyControl
block|{
specifier|private
specifier|volatile
name|long
name|memstoreRead
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|memstoreWrite
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|Object
name|readWaiters
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|// This is the pending queue of writes.
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|WriteEntry
argument_list|>
name|writeQueue
init|=
operator|new
name|LinkedList
argument_list|<
name|WriteEntry
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|Long
argument_list|>
name|perThreadReadPoint
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|long
name|getThreadReadPoint
parameter_list|()
block|{
return|return
name|perThreadReadPoint
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|setThreadReadPoint
parameter_list|(
name|long
name|readPoint
parameter_list|)
block|{
name|perThreadReadPoint
operator|.
name|set
argument_list|(
name|readPoint
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|long
name|resetThreadReadPoint
parameter_list|(
name|ReadWriteConsistencyControl
name|rwcc
parameter_list|)
block|{
name|perThreadReadPoint
operator|.
name|set
argument_list|(
name|rwcc
operator|.
name|memstoreReadPoint
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|getThreadReadPoint
argument_list|()
return|;
block|}
specifier|public
name|WriteEntry
name|beginMemstoreInsert
parameter_list|()
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|long
name|nextWriteNumber
init|=
operator|++
name|memstoreWrite
decl_stmt|;
name|WriteEntry
name|e
init|=
operator|new
name|WriteEntry
argument_list|(
name|nextWriteNumber
argument_list|)
decl_stmt|;
name|writeQueue
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
name|e
return|;
block|}
block|}
specifier|public
name|void
name|completeMemstoreInsert
parameter_list|(
name|WriteEntry
name|e
parameter_list|)
block|{
synchronized|synchronized
init|(
name|writeQueue
init|)
block|{
name|e
operator|.
name|markCompleted
argument_list|()
expr_stmt|;
name|long
name|nextReadValue
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|ranOnce
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|writeQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ranOnce
operator|=
literal|true
expr_stmt|;
name|WriteEntry
name|queueFirst
init|=
name|writeQueue
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextReadValue
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|nextReadValue
operator|+
literal|1
operator|!=
name|queueFirst
operator|.
name|getWriteNumber
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"invariant in completeMemstoreInsert violated, prev: "
operator|+
name|nextReadValue
operator|+
literal|" next: "
operator|+
name|queueFirst
operator|.
name|getWriteNumber
argument_list|()
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|queueFirst
operator|.
name|isCompleted
argument_list|()
condition|)
block|{
name|nextReadValue
operator|=
name|queueFirst
operator|.
name|getWriteNumber
argument_list|()
expr_stmt|;
name|writeQueue
operator|.
name|removeFirst
argument_list|()
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|ranOnce
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"never was a first"
argument_list|)
throw|;
block|}
if|if
condition|(
name|nextReadValue
operator|>
literal|0
condition|)
block|{
name|memstoreRead
operator|=
name|nextReadValue
expr_stmt|;
synchronized|synchronized
init|(
name|readWaiters
init|)
block|{
name|readWaiters
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|boolean
name|interrupted
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|memstoreRead
operator|<
name|e
operator|.
name|getWriteNumber
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|readWaiters
init|)
block|{
try|try
block|{
name|readWaiters
operator|.
name|wait
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// We were interrupted... finish the loop -- i.e. cleanup --and then
comment|// on our way out, reset the interrupt flag.
name|interrupted
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|interrupted
condition|)
name|Thread
operator|.
name|currentThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|memstoreReadPoint
parameter_list|()
block|{
return|return
name|memstoreRead
return|;
block|}
specifier|public
specifier|static
class|class
name|WriteEntry
block|{
specifier|private
name|long
name|writeNumber
decl_stmt|;
specifier|private
name|boolean
name|completed
init|=
literal|false
decl_stmt|;
name|WriteEntry
parameter_list|(
name|long
name|writeNumber
parameter_list|)
block|{
name|this
operator|.
name|writeNumber
operator|=
name|writeNumber
expr_stmt|;
block|}
name|void
name|markCompleted
parameter_list|()
block|{
name|this
operator|.
name|completed
operator|=
literal|true
expr_stmt|;
block|}
name|boolean
name|isCompleted
parameter_list|()
block|{
return|return
name|this
operator|.
name|completed
return|;
block|}
name|long
name|getWriteNumber
parameter_list|()
block|{
return|return
name|this
operator|.
name|writeNumber
return|;
block|}
block|}
block|}
end_class

end_unit

