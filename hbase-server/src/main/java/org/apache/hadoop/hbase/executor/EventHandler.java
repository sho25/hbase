begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|executor
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Server
import|;
end_import

begin_import
import|import
name|org
operator|.
name|cloudera
operator|.
name|htrace
operator|.
name|Sampler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|cloudera
operator|.
name|htrace
operator|.
name|Span
import|;
end_import

begin_import
import|import
name|org
operator|.
name|cloudera
operator|.
name|htrace
operator|.
name|Trace
import|;
end_import

begin_comment
comment|/**  * Abstract base class for all HBase event handlers. Subclasses should  * implement the {@link #process()} and {@link #prepare()} methods.  Subclasses  * should also do all necessary checks up in their prepare() if possible -- check  * table exists, is disabled, etc. -- so they fail fast rather than later when process  * is running.  Do it this way because process be invoked directly but event  * handlers are also  * run in an executor context -- i.e. asynchronously -- and in this case,  * exceptions thrown at process time will not be seen by the invoker, not till  * we implement a call-back mechanism so the client can pick them up later.  *<p>  * Event handlers have an {@link EventType}.  * {@link EventType} is a list of ALL handler event types.  We need to keep  * a full list in one place -- and as enums is a good shorthand for an  * implemenations -- because event handlers can be passed to executors when  * they are to be run asynchronously. The  * hbase executor, see ExecutorService, has a switch for passing  * event type to executor.  *<p>  * Event listeners can be installed and will be called pre- and post- process if  * this EventHandler is run in a Thread (its a Runnable so if its {@link #run()}  * method gets called).  Implement  * {@link EventHandlerListener}s, and registering using  * {@link #setListener(EventHandlerListener)}.  * @see ExecutorService  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|EventHandler
implements|implements
name|Runnable
implements|,
name|Comparable
argument_list|<
name|Runnable
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|EventHandler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// type of event this object represents
specifier|protected
name|EventType
name|eventType
decl_stmt|;
specifier|protected
name|Server
name|server
decl_stmt|;
comment|// sequence id generator for default FIFO ordering of events
specifier|protected
specifier|static
specifier|final
name|AtomicLong
name|seqids
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// sequence id for this event
specifier|private
specifier|final
name|long
name|seqid
decl_stmt|;
comment|// Listener to call pre- and post- processing.  May be null.
specifier|private
name|EventHandlerListener
name|listener
decl_stmt|;
comment|// Time to wait for events to happen, should be kept short
specifier|protected
name|int
name|waitingTimeForEvents
decl_stmt|;
specifier|private
specifier|final
name|Span
name|parent
decl_stmt|;
comment|/**    * This interface provides pre- and post-process hooks for events.    */
specifier|public
interface|interface
name|EventHandlerListener
block|{
comment|/**      * Called before any event is processed      * @param event The event handler whose process method is about to be called.      */
name|void
name|beforeProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
function_decl|;
comment|/**      * Called after any event is processed      * @param event The event handler whose process method is about to be called.      */
name|void
name|afterProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
function_decl|;
block|}
comment|/**    * Default base class constructor.    */
specifier|public
name|EventHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|EventType
name|eventType
parameter_list|)
block|{
name|this
operator|.
name|parent
operator|=
name|Trace
operator|.
name|currentTrace
argument_list|()
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|eventType
operator|=
name|eventType
expr_stmt|;
name|seqid
operator|=
name|seqids
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|waitingTimeForEvents
operator|=
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.master.event.waiting.time"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Event handlers should do all the necessary checks in this method (rather than    * in the constructor, or in process()) so that the caller, which is mostly executed    * in the ipc context can fail fast. Process is executed async from the client ipc,    * so this method gives a quick chance to do some basic checks.    * Should be called after constructing the EventHandler, and before process().    * @return the instance of this class    * @throws Exception when something goes wrong    */
specifier|public
name|EventHandler
name|prepare
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|this
return|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
name|Span
name|chunk
init|=
name|Trace
operator|.
name|startSpan
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|parent
argument_list|,
name|Sampler
operator|.
name|ALWAYS
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|getListener
argument_list|()
operator|!=
literal|null
condition|)
name|getListener
argument_list|()
operator|.
name|beforeProcess
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|process
argument_list|()
expr_stmt|;
if|if
condition|(
name|getListener
argument_list|()
operator|!=
literal|null
condition|)
name|getListener
argument_list|()
operator|.
name|afterProcess
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught throwable while processing event "
operator|+
name|eventType
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|chunk
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * This method is the main processing loop to be implemented by the various    * subclasses.    * @throws IOException    */
specifier|public
specifier|abstract
name|void
name|process
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Return the event type    * @return The event type.    */
specifier|public
name|EventType
name|getEventType
parameter_list|()
block|{
return|return
name|this
operator|.
name|eventType
return|;
block|}
comment|/**    * Get the priority level for this handler instance.  This uses natural    * ordering so lower numbers are higher priority.    *<p>    * Lowest priority is Integer.MAX_VALUE.  Highest priority is 0.    *<p>    * Subclasses should override this method to allow prioritizing handlers.    *<p>    * Handlers with the same priority are handled in FIFO order.    *<p>    * @return Integer.MAX_VALUE by default, override to set higher priorities    */
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
comment|/**    * @return This events' sequence id.    */
specifier|public
name|long
name|getSeqid
parameter_list|()
block|{
return|return
name|this
operator|.
name|seqid
return|;
block|}
comment|/**    * Default prioritized runnable comparator which implements a FIFO ordering.    *<p>    * Subclasses should not override this.  Instead, if they want to implement    * priority beyond FIFO, they should override {@link #getPriority()}.    */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Runnable
name|o
parameter_list|)
block|{
name|EventHandler
name|eh
init|=
operator|(
name|EventHandler
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|getPriority
argument_list|()
operator|!=
name|eh
operator|.
name|getPriority
argument_list|()
condition|)
block|{
return|return
operator|(
name|getPriority
argument_list|()
operator|<
name|eh
operator|.
name|getPriority
argument_list|()
operator|)
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
return|return
operator|(
name|this
operator|.
name|seqid
operator|<
name|eh
operator|.
name|seqid
operator|)
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
comment|/**    * @return Current listener or null if none set.    */
specifier|public
specifier|synchronized
name|EventHandlerListener
name|getListener
parameter_list|()
block|{
return|return
name|listener
return|;
block|}
comment|/**    * @param listener Listener to call pre- and post- {@link #process()}.    */
specifier|public
specifier|synchronized
name|void
name|setListener
parameter_list|(
name|EventHandlerListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Event #"
operator|+
name|getSeqid
argument_list|()
operator|+
literal|" of type "
operator|+
name|eventType
operator|+
literal|" ("
operator|+
name|getInformativeName
argument_list|()
operator|+
literal|")"
return|;
block|}
comment|/**    * Event implementations should override thie class to provide an    * informative name about what event they are handling. For example,    * event-specific information such as which region or server is    * being processed should be included if possible.    */
specifier|public
name|String
name|getInformativeName
parameter_list|()
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

