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
name|client
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
name|TimeUnit
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
name|ServerName
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
name|TableName
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
name|log
operator|.
name|HBaseMarkers
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
name|FutureUtils
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Closeables
import|;
end_import

begin_comment
comment|/**  * The connection implementation based on {@link AsyncConnection}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ConnectionOverAsyncConnection
implements|implements
name|Connection
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
name|ConnectionOverAsyncConnection
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|aborted
init|=
literal|false
decl_stmt|;
specifier|private
specifier|volatile
name|ExecutorService
name|batchPool
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|final
name|AsyncConnectionImpl
name|conn
decl_stmt|;
comment|/**    * @deprecated we can not implement all the related stuffs at once so keep it here for now, will    *             remove it after we implement all the stuffs, like Admin, RegionLocator, etc.    */
annotation|@
name|Deprecated
specifier|private
specifier|final
name|ConnectionImplementation
name|oldConn
decl_stmt|;
specifier|private
specifier|final
name|ConnectionConfiguration
name|connConf
decl_stmt|;
name|ConnectionOverAsyncConnection
parameter_list|(
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|ConnectionImplementation
name|oldConn
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|oldConn
operator|=
name|oldConn
expr_stmt|;
name|this
operator|.
name|connConf
operator|=
operator|new
name|ConnectionConfiguration
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|error
parameter_list|)
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|HBaseMarkers
operator|.
name|FATAL
argument_list|,
name|why
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
name|HBaseMarkers
operator|.
name|FATAL
argument_list|,
name|why
argument_list|)
expr_stmt|;
block|}
name|aborted
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|Closeables
operator|.
name|close
argument_list|(
name|this
argument_list|,
literal|true
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
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|aborted
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conn
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BufferedMutator
name|getBufferedMutator
parameter_list|(
name|BufferedMutatorParams
name|params
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|oldConn
operator|.
name|getBufferedMutator
argument_list|(
name|params
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|oldConn
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearRegionLocationCache
parameter_list|()
block|{
name|conn
operator|.
name|clearRegionLocationCache
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|oldConn
operator|.
name|getAdmin
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// will be called from AsyncConnection, to avoid infinite loop as in the above method we will call
comment|// AsyncConnection.close.
name|void
name|closeConnImpl
parameter_list|()
block|{
name|ExecutorService
name|batchPool
init|=
name|this
operator|.
name|batchPool
decl_stmt|;
if|if
condition|(
name|batchPool
operator|!=
literal|null
condition|)
block|{
name|ConnectionUtils
operator|.
name|shutdownPool
argument_list|(
name|batchPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|batchPool
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|conn
operator|.
name|isClosed
argument_list|()
return|;
block|}
specifier|private
name|ExecutorService
name|getBatchPool
parameter_list|()
block|{
if|if
condition|(
name|batchPool
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|batchPool
operator|==
literal|null
condition|)
block|{
name|int
name|threads
init|=
name|conn
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.hconnection.threads.max"
argument_list|,
literal|256
argument_list|)
decl_stmt|;
name|this
operator|.
name|batchPool
operator|=
name|ConnectionUtils
operator|.
name|getThreadPool
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|threads
argument_list|,
name|threads
argument_list|,
parameter_list|()
lambda|->
name|toString
argument_list|()
operator|+
literal|"-shared"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|this
operator|.
name|batchPool
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|getTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
operator|new
name|TableBuilderBase
argument_list|(
name|tableName
argument_list|,
name|connConf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Table
name|build
parameter_list|()
block|{
name|ExecutorService
name|p
init|=
name|pool
operator|!=
literal|null
condition|?
name|pool
else|:
name|getBatchPool
argument_list|()
decl_stmt|;
return|return
operator|new
name|TableOverAsyncTable
argument_list|(
name|conn
argument_list|,
name|conn
operator|.
name|getTableBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setRpcTimeout
argument_list|(
name|rpcTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|setReadRpcTimeout
argument_list|(
name|readRpcTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|setWriteRpcTimeout
argument_list|(
name|writeRpcTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|setOperationTimeout
argument_list|(
name|operationTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|p
argument_list|)
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncConnection
name|toAsyncConnection
parameter_list|()
block|{
return|return
name|conn
return|;
block|}
annotation|@
name|Override
specifier|public
name|Hbck
name|getHbck
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|FutureUtils
operator|.
name|get
argument_list|(
name|conn
operator|.
name|getHbck
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Hbck
name|getHbck
parameter_list|(
name|ServerName
name|masterServer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|conn
operator|.
name|getHbck
argument_list|(
name|masterServer
argument_list|)
return|;
block|}
comment|/**    * An identifier that will remain the same for a given connection.    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"connection-over-async-connection-0x"
operator|+
name|Integer
operator|.
name|toHexString
argument_list|(
name|hashCode
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

