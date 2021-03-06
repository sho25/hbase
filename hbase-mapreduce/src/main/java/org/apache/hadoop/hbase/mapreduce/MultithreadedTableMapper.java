begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|ExecutorService
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
name|Executors
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
name|conf
operator|.
name|Configuration
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
name|client
operator|.
name|Result
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|mapreduce
operator|.
name|Counter
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
name|mapreduce
operator|.
name|InputSplit
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|MapContext
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
name|mapreduce
operator|.
name|Mapper
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
name|mapreduce
operator|.
name|OutputCommitter
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
name|mapreduce
operator|.
name|RecordReader
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
name|mapreduce
operator|.
name|RecordWriter
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
name|mapreduce
operator|.
name|StatusReporter
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|TaskAttemptID
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Multithreaded implementation for @link org.apache.hbase.mapreduce.TableMapper  *<p>  * It can be used instead when the Map operation is not CPU  * bound in order to improve throughput.  *<p>  * Mapper implementations using this MapRunnable must be thread-safe.  *<p>  * The Map-Reduce job has to be configured with the mapper to use via  * {@link #setMapperClass} and the number of thread the thread-pool can use with the  * {@link #getNumberOfThreads} method. The default value is 10 threads.  *<p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MultithreadedTableMapper
parameter_list|<
name|K2
parameter_list|,
name|V2
parameter_list|>
extends|extends
name|TableMapper
argument_list|<
name|K2
argument_list|,
name|V2
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MultithreadedTableMapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|Mapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|,
name|K2
argument_list|,
name|V2
argument_list|>
argument_list|>
name|mapClass
decl_stmt|;
specifier|private
name|Context
name|outer
decl_stmt|;
specifier|private
name|ExecutorService
name|executor
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NUMBER_OF_THREADS
init|=
literal|"hbase.mapreduce.multithreadedmapper.threads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAPPER_CLASS
init|=
literal|"hbase.mapreduce.multithreadedmapper.mapclass"
decl_stmt|;
comment|/**    * The number of threads in the thread pool that will run the map function.    * @param job the job    * @return the number of threads    */
specifier|public
specifier|static
name|int
name|getNumberOfThreads
parameter_list|(
name|JobContext
name|job
parameter_list|)
block|{
return|return
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|NUMBER_OF_THREADS
argument_list|,
literal|10
argument_list|)
return|;
block|}
comment|/**    * Set the number of threads in the pool for running maps.    * @param job the job to modify    * @param threads the new number of threads    */
specifier|public
specifier|static
name|void
name|setNumberOfThreads
parameter_list|(
name|Job
name|job
parameter_list|,
name|int
name|threads
parameter_list|)
block|{
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|NUMBER_OF_THREADS
argument_list|,
name|threads
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the application's mapper class.    * @param<K2> the map's output key type    * @param<V2> the map's output value type    * @param job the job    * @return the mapper class to run    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|K2
parameter_list|,
name|V2
parameter_list|>
name|Class
argument_list|<
name|Mapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|,
name|K2
argument_list|,
name|V2
argument_list|>
argument_list|>
name|getMapperClass
parameter_list|(
name|JobContext
name|job
parameter_list|)
block|{
return|return
operator|(
name|Class
argument_list|<
name|Mapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|,
name|K2
argument_list|,
name|V2
argument_list|>
argument_list|>
operator|)
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getClass
argument_list|(
name|MAPPER_CLASS
argument_list|,
name|Mapper
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Set the application's mapper class.    * @param<K2> the map output key type    * @param<V2> the map output value type    * @param job the job to modify    * @param cls the class to use as the mapper    */
specifier|public
specifier|static
parameter_list|<
name|K2
parameter_list|,
name|V2
parameter_list|>
name|void
name|setMapperClass
parameter_list|(
name|Job
name|job
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Mapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|,
name|K2
argument_list|,
name|V2
argument_list|>
argument_list|>
name|cls
parameter_list|)
block|{
if|if
condition|(
name|MultithreadedTableMapper
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|cls
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't have recursive "
operator|+
literal|"MultithreadedTableMapper instances."
argument_list|)
throw|;
block|}
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setClass
argument_list|(
name|MAPPER_CLASS
argument_list|,
name|cls
argument_list|,
name|Mapper
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run the application's maps using a thread pool.    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|outer
operator|=
name|context
expr_stmt|;
name|int
name|numberOfThreads
init|=
name|getNumberOfThreads
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|mapClass
operator|=
name|getMapperClass
argument_list|(
name|context
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Configuring multithread runner to use "
operator|+
name|numberOfThreads
operator|+
literal|" threads"
argument_list|)
expr_stmt|;
block|}
name|executor
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|numberOfThreads
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numberOfThreads
condition|;
operator|++
name|i
control|)
block|{
name|MapRunner
name|thread
init|=
operator|new
name|MapRunner
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|thread
argument_list|)
expr_stmt|;
block|}
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|executor
operator|.
name|isTerminated
argument_list|()
condition|)
block|{
comment|// wait till all the threads are done
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|SubMapRecordReader
extends|extends
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|private
name|ImmutableBytesWritable
name|key
decl_stmt|;
specifier|private
name|Result
name|value
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|conf
operator|=
name|context
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|outer
init|)
block|{
if|if
condition|(
operator|!
name|outer
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|key
operator|=
name|ReflectionUtils
operator|.
name|copy
argument_list|(
name|outer
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|outer
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|value
operator|=
name|ReflectionUtils
operator|.
name|copy
argument_list|(
name|conf
argument_list|,
name|outer
operator|.
name|getCurrentValue
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
specifier|public
name|ImmutableBytesWritable
name|getCurrentKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|getCurrentValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
block|}
specifier|private
class|class
name|SubMapRecordWriter
extends|extends
name|RecordWriter
argument_list|<
name|K2
argument_list|,
name|V2
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|K2
name|key
parameter_list|,
name|V2
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|outer
init|)
block|{
name|outer
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
class|class
name|SubMapStatusReporter
extends|extends
name|StatusReporter
block|{
annotation|@
name|Override
specifier|public
name|Counter
name|getCounter
parameter_list|(
name|Enum
argument_list|<
name|?
argument_list|>
name|name
parameter_list|)
block|{
return|return
name|outer
operator|.
name|getCounter
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Counter
name|getCounter
parameter_list|(
name|String
name|group
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|outer
operator|.
name|getCounter
argument_list|(
name|group
argument_list|,
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|progress
parameter_list|()
block|{
name|outer
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStatus
parameter_list|(
name|String
name|status
parameter_list|)
block|{
name|outer
operator|.
name|setStatus
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"REC_CATCH_EXCEPTION"
argument_list|,
name|justification
operator|=
literal|"Don't understand why FB is complaining about this one. We do throw exception"
argument_list|)
specifier|private
class|class
name|MapRunner
implements|implements
name|Runnable
block|{
specifier|private
name|Mapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|,
name|K2
argument_list|,
name|V2
argument_list|>
name|mapper
decl_stmt|;
specifier|private
name|Context
name|subcontext
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
name|MapRunner
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|mapper
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|mapClass
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Constructor
name|c
init|=
name|context
operator|.
name|getClass
argument_list|()
operator|.
name|getConstructor
argument_list|(
name|Mapper
operator|.
name|class
argument_list|,
name|Configuration
operator|.
name|class
argument_list|,
name|TaskAttemptID
operator|.
name|class
argument_list|,
name|RecordReader
operator|.
name|class
argument_list|,
name|RecordWriter
operator|.
name|class
argument_list|,
name|OutputCommitter
operator|.
name|class
argument_list|,
name|StatusReporter
operator|.
name|class
argument_list|,
name|InputSplit
operator|.
name|class
argument_list|)
decl_stmt|;
name|c
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|subcontext
operator|=
operator|(
name|Context
operator|)
name|c
operator|.
name|newInstance
argument_list|(
name|mapper
argument_list|,
name|outer
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|outer
operator|.
name|getTaskAttemptID
argument_list|()
argument_list|,
operator|new
name|SubMapRecordReader
argument_list|()
argument_list|,
operator|new
name|SubMapRecordWriter
argument_list|()
argument_list|,
name|context
operator|.
name|getOutputCommitter
argument_list|()
argument_list|,
operator|new
name|SubMapStatusReporter
argument_list|()
argument_list|,
name|outer
operator|.
name|getInputSplit
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|Constructor
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.mapreduce.task.MapContextImpl"
argument_list|)
operator|.
name|getConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|,
name|TaskAttemptID
operator|.
name|class
argument_list|,
name|RecordReader
operator|.
name|class
argument_list|,
name|RecordWriter
operator|.
name|class
argument_list|,
name|OutputCommitter
operator|.
name|class
argument_list|,
name|StatusReporter
operator|.
name|class
argument_list|,
name|InputSplit
operator|.
name|class
argument_list|)
decl_stmt|;
name|c
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|MapContext
name|mc
init|=
operator|(
name|MapContext
operator|)
name|c
operator|.
name|newInstance
argument_list|(
name|outer
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|outer
operator|.
name|getTaskAttemptID
argument_list|()
argument_list|,
operator|new
name|SubMapRecordReader
argument_list|()
argument_list|,
operator|new
name|SubMapRecordWriter
argument_list|()
argument_list|,
name|context
operator|.
name|getOutputCommitter
argument_list|()
argument_list|,
operator|new
name|SubMapStatusReporter
argument_list|()
argument_list|,
name|outer
operator|.
name|getInputSplit
argument_list|()
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|wrappedMapperClass
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.mapreduce.lib.map.WrappedMapper"
argument_list|)
decl_stmt|;
name|Method
name|getMapContext
init|=
name|wrappedMapperClass
operator|.
name|getMethod
argument_list|(
literal|"getMapContext"
argument_list|,
name|MapContext
operator|.
name|class
argument_list|)
decl_stmt|;
name|subcontext
operator|=
operator|(
name|Context
operator|)
name|getMapContext
operator|.
name|invoke
argument_list|(
name|wrappedMapperClass
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
argument_list|,
name|mc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ee
parameter_list|)
block|{
comment|// FindBugs: REC_CATCH_EXCEPTION
comment|// rethrow as IOE
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|mapper
operator|.
name|run
argument_list|(
name|subcontext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem in running map."
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

