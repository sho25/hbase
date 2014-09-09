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
name|thrift2
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift2
operator|.
name|ThriftUtilities
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBaseHelper
operator|.
name|byteBufferToByteArray
import|;
end_import

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
name|InvocationHandler
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
name|InvocationTargetException
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
name|lang
operator|.
name|reflect
operator|.
name|Proxy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|Callable
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
name|ConcurrentHashMap
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
name|ExecutionException
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
name|TimeUnit
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
name|AtomicInteger
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
name|HTableFactory
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
name|HTableInterface
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
name|ResultScanner
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
name|Table
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
name|security
operator|.
name|UserProvider
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
name|thrift
operator|.
name|ThriftMetrics
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
name|thrift2
operator|.
name|generated
operator|.
name|*
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|ConnectionCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|Cache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|CacheBuilder
import|;
end_import

begin_comment
comment|/**  * This class is a glue object that connects Thrift RPC calls to the HBase client API primarily  * defined in the HTableInterface.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|ThriftHBaseServiceHandler
implements|implements
name|THBaseService
operator|.
name|Iface
block|{
comment|// TODO: Size of pool configuraple
specifier|private
specifier|final
name|Cache
argument_list|<
name|String
argument_list|,
name|HTablePool
argument_list|>
name|htablePools
decl_stmt|;
specifier|private
specifier|final
name|Callable
argument_list|<
name|?
extends|extends
name|HTablePool
argument_list|>
name|htablePoolCreater
decl_stmt|;
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
name|ThriftHBaseServiceHandler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// nextScannerId and scannerMap are used to manage scanner state
comment|// TODO: Cleanup thread for Scanners, Scanner id wrap
specifier|private
specifier|final
name|AtomicInteger
name|nextScannerId
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|ResultScanner
argument_list|>
name|scannerMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Integer
argument_list|,
name|ResultScanner
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConnectionCache
name|connectionCache
decl_stmt|;
specifier|private
specifier|final
name|HTableFactory
name|tableFactory
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxPoolSize
decl_stmt|;
specifier|static
specifier|final
name|String
name|CLEANUP_INTERVAL
init|=
literal|"hbase.thrift.connection.cleanup-interval"
decl_stmt|;
specifier|static
specifier|final
name|String
name|MAX_IDLETIME
init|=
literal|"hbase.thrift.connection.max-idletime"
decl_stmt|;
specifier|public
specifier|static
name|THBaseService
operator|.
name|Iface
name|newInstance
parameter_list|(
name|THBaseService
operator|.
name|Iface
name|handler
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|)
block|{
return|return
operator|(
name|THBaseService
operator|.
name|Iface
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|handler
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|THBaseService
operator|.
name|Iface
operator|.
name|class
block|}
argument_list|,
operator|new
name|THBaseServiceMetricsProxy
argument_list|(
name|handler
argument_list|,
name|metrics
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|THBaseServiceMetricsProxy
implements|implements
name|InvocationHandler
block|{
specifier|private
specifier|final
name|THBaseService
operator|.
name|Iface
name|handler
decl_stmt|;
specifier|private
specifier|final
name|ThriftMetrics
name|metrics
decl_stmt|;
specifier|private
name|THBaseServiceMetricsProxy
parameter_list|(
name|THBaseService
operator|.
name|Iface
name|handler
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|m
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|Object
name|result
decl_stmt|;
try|try
block|{
name|long
name|start
init|=
name|now
argument_list|()
decl_stmt|;
name|result
operator|=
name|m
operator|.
name|invoke
argument_list|(
name|handler
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|int
name|processTime
init|=
call|(
name|int
call|)
argument_list|(
name|now
argument_list|()
operator|-
name|start
argument_list|)
decl_stmt|;
name|metrics
operator|.
name|incMethodTime
argument_list|(
name|m
operator|.
name|getName
argument_list|()
argument_list|,
name|processTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
name|e
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unexpected invocation exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
block|}
specifier|private
specifier|static
name|long
name|now
parameter_list|()
block|{
return|return
name|System
operator|.
name|nanoTime
argument_list|()
return|;
block|}
name|ThriftHBaseServiceHandler
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|UserProvider
name|userProvider
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|cleanInterval
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CLEANUP_INTERVAL
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|maxIdleTime
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_IDLETIME
argument_list|,
literal|10
operator|*
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|connectionCache
operator|=
operator|new
name|ConnectionCache
argument_list|(
name|conf
argument_list|,
name|userProvider
argument_list|,
name|cleanInterval
argument_list|,
name|maxIdleTime
argument_list|)
expr_stmt|;
name|tableFactory
operator|=
operator|new
name|HTableFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|HTableInterface
name|createHTableInterface
parameter_list|(
name|Configuration
name|config
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
block|{
try|try
block|{
return|return
name|connectionCache
operator|.
name|getTable
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
block|}
expr_stmt|;
name|htablePools
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
name|maxIdleTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|softValues
argument_list|()
operator|.
name|concurrencyLevel
argument_list|(
literal|4
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|maxPoolSize
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.thrift.htablepool.size.max"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|htablePoolCreater
operator|=
operator|new
name|Callable
argument_list|<
name|HTablePool
argument_list|>
argument_list|()
block|{
specifier|public
name|HTablePool
name|call
parameter_list|()
block|{
return|return
operator|new
name|HTablePool
argument_list|(
name|conf
argument_list|,
name|maxPoolSize
argument_list|,
name|tableFactory
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
specifier|private
name|Table
name|getTable
parameter_list|(
name|ByteBuffer
name|tableName
parameter_list|)
block|{
name|String
name|currentUser
init|=
name|connectionCache
operator|.
name|getEffectiveUser
argument_list|()
decl_stmt|;
try|try
block|{
name|HTablePool
name|htablePool
init|=
name|htablePools
operator|.
name|get
argument_list|(
name|currentUser
argument_list|,
name|htablePoolCreater
argument_list|)
decl_stmt|;
return|return
name|htablePool
operator|.
name|getTable
argument_list|(
name|byteBufferToByteArray
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ee
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ee
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|closeTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|TIOError
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|TIOError
name|getTIOError
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|TIOError
name|err
init|=
operator|new
name|TIOError
argument_list|()
decl_stmt|;
name|err
operator|.
name|setMessage
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|err
return|;
block|}
comment|/**    * Assigns a unique ID to the scanner and adds the mapping to an internal HashMap.    * @param scanner to add    * @return Id for this Scanner    */
specifier|private
name|int
name|addScanner
parameter_list|(
name|ResultScanner
name|scanner
parameter_list|)
block|{
name|int
name|id
init|=
name|nextScannerId
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
name|scannerMap
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|scanner
argument_list|)
expr_stmt|;
return|return
name|id
return|;
block|}
comment|/**    * Returns the Scanner associated with the specified Id.    * @param id of the Scanner to get    * @return a Scanner, or null if the Id is invalid    */
specifier|private
name|ResultScanner
name|getScanner
parameter_list|(
name|int
name|id
parameter_list|)
block|{
return|return
name|scannerMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
name|void
name|setEffectiveUser
parameter_list|(
name|String
name|effectiveUser
parameter_list|)
block|{
name|connectionCache
operator|.
name|setEffectiveUser
argument_list|(
name|effectiveUser
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes the scanner associated with the specified ID from the internal HashMap.    * @param id of the Scanner to remove    * @return the removed Scanner, or<code>null</code> if the Id is invalid    */
specifier|protected
name|ResultScanner
name|removeScanner
parameter_list|(
name|int
name|id
parameter_list|)
block|{
return|return
name|scannerMap
operator|.
name|remove
argument_list|(
name|id
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|exists
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TGet
name|get
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|htable
operator|.
name|exists
argument_list|(
name|getFromThrift
argument_list|(
name|get
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|TResult
name|get
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TGet
name|get
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|resultFromHBase
argument_list|(
name|htable
operator|.
name|get
argument_list|(
name|getFromThrift
argument_list|(
name|get
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|TResult
argument_list|>
name|getMultiple
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|List
argument_list|<
name|TGet
argument_list|>
name|gets
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|resultsFromHBase
argument_list|(
name|htable
operator|.
name|get
argument_list|(
name|getsFromThrift
argument_list|(
name|gets
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TPut
name|put
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
name|htable
operator|.
name|put
argument_list|(
name|putFromThrift
argument_list|(
name|put
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|ByteBuffer
name|row
parameter_list|,
name|ByteBuffer
name|family
parameter_list|,
name|ByteBuffer
name|qualifier
parameter_list|,
name|ByteBuffer
name|value
parameter_list|,
name|TPut
name|put
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|htable
operator|.
name|checkAndPut
argument_list|(
name|byteBufferToByteArray
argument_list|(
name|row
argument_list|)
argument_list|,
name|byteBufferToByteArray
argument_list|(
name|family
argument_list|)
argument_list|,
name|byteBufferToByteArray
argument_list|(
name|qualifier
argument_list|)
argument_list|,
operator|(
name|value
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|byteBufferToByteArray
argument_list|(
name|value
argument_list|)
argument_list|,
name|putFromThrift
argument_list|(
name|put
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|putMultiple
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|List
argument_list|<
name|TPut
argument_list|>
name|puts
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
name|htable
operator|.
name|put
argument_list|(
name|putsFromThrift
argument_list|(
name|puts
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|deleteSingle
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TDelete
name|deleteSingle
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
name|htable
operator|.
name|delete
argument_list|(
name|deleteFromThrift
argument_list|(
name|deleteSingle
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|TDelete
argument_list|>
name|deleteMultiple
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|List
argument_list|<
name|TDelete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
name|htable
operator|.
name|delete
argument_list|(
name|deletesFromThrift
argument_list|(
name|deletes
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|ByteBuffer
name|row
parameter_list|,
name|ByteBuffer
name|family
parameter_list|,
name|ByteBuffer
name|qualifier
parameter_list|,
name|ByteBuffer
name|value
parameter_list|,
name|TDelete
name|deleteSingle
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|htable
operator|.
name|checkAndDelete
argument_list|(
name|byteBufferToByteArray
argument_list|(
name|row
argument_list|)
argument_list|,
name|byteBufferToByteArray
argument_list|(
name|family
argument_list|)
argument_list|,
name|byteBufferToByteArray
argument_list|(
name|qualifier
argument_list|)
argument_list|,
literal|null
argument_list|,
name|deleteFromThrift
argument_list|(
name|deleteSingle
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|htable
operator|.
name|checkAndDelete
argument_list|(
name|byteBufferToByteArray
argument_list|(
name|row
argument_list|)
argument_list|,
name|byteBufferToByteArray
argument_list|(
name|family
argument_list|)
argument_list|,
name|byteBufferToByteArray
argument_list|(
name|qualifier
argument_list|)
argument_list|,
name|byteBufferToByteArray
argument_list|(
name|value
argument_list|)
argument_list|,
name|deleteFromThrift
argument_list|(
name|deleteSingle
argument_list|)
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|TResult
name|increment
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TIncrement
name|increment
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|resultFromHBase
argument_list|(
name|htable
operator|.
name|increment
argument_list|(
name|incrementFromThrift
argument_list|(
name|increment
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|TResult
name|append
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TAppend
name|append
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|resultFromHBase
argument_list|(
name|htable
operator|.
name|append
argument_list|(
name|appendFromThrift
argument_list|(
name|append
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|openScanner
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TScan
name|scan
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|ResultScanner
name|resultScanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|resultScanner
operator|=
name|htable
operator|.
name|getScanner
argument_list|(
name|scanFromThrift
argument_list|(
name|scan
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
return|return
name|addScanner
argument_list|(
name|resultScanner
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|TResult
argument_list|>
name|getScannerRows
parameter_list|(
name|int
name|scannerId
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TIllegalArgument
throws|,
name|TException
block|{
name|ResultScanner
name|scanner
init|=
name|getScanner
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanner
operator|==
literal|null
condition|)
block|{
name|TIllegalArgument
name|ex
init|=
operator|new
name|TIllegalArgument
argument_list|()
decl_stmt|;
name|ex
operator|.
name|setMessage
argument_list|(
literal|"Invalid scanner Id"
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
try|try
block|{
return|return
name|resultsFromHBase
argument_list|(
name|scanner
operator|.
name|next
argument_list|(
name|numRows
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|TResult
argument_list|>
name|getScannerResults
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TScan
name|scan
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TResult
argument_list|>
name|results
init|=
literal|null
decl_stmt|;
name|ResultScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|scanner
operator|=
name|htable
operator|.
name|getScanner
argument_list|(
name|scanFromThrift
argument_list|(
name|scan
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|=
name|resultsFromHBase
argument_list|(
name|scanner
operator|.
name|next
argument_list|(
name|numRows
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeScanner
parameter_list|(
name|int
name|scannerId
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TIllegalArgument
throws|,
name|TException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"scannerClose: id="
operator|+
name|scannerId
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|getScanner
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanner
operator|==
literal|null
condition|)
block|{
name|String
name|message
init|=
literal|"scanner ID is invalid"
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|TIllegalArgument
name|ex
init|=
operator|new
name|TIllegalArgument
argument_list|()
decl_stmt|;
name|ex
operator|.
name|setMessage
argument_list|(
literal|"Invalid scanner Id"
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|removeScanner
argument_list|(
name|scannerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|mutateRow
parameter_list|(
name|ByteBuffer
name|table
parameter_list|,
name|TRowMutations
name|rowMutations
parameter_list|)
throws|throws
name|TIOError
throws|,
name|TException
block|{
name|Table
name|htable
init|=
name|getTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
name|htable
operator|.
name|mutateRow
argument_list|(
name|rowMutationsFromThrift
argument_list|(
name|rowMutations
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|getTIOError
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|closeTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

