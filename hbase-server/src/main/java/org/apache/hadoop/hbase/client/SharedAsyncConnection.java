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
name|CompletableFuture
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Wraps a {@link AsyncConnection} to make it can't be closed.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SharedAsyncConnection
implements|implements
name|AsyncConnection
block|{
specifier|private
specifier|final
name|AsyncConnection
name|conn
decl_stmt|;
specifier|public
name|SharedAsyncConnection
parameter_list|(
name|AsyncConnection
name|conn
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
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
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Shared connection"
argument_list|)
throw|;
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
name|AsyncTableRegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|conn
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
name|AsyncTableBuilder
argument_list|<
name|AdvancedScanResultConsumer
argument_list|>
name|getTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|conn
operator|.
name|getTableBuilder
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilder
argument_list|<
name|ScanResultConsumer
argument_list|>
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
name|conn
operator|.
name|getTableBuilder
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncAdminBuilder
name|getAdminBuilder
parameter_list|()
block|{
return|return
name|conn
operator|.
name|getAdminBuilder
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncAdminBuilder
name|getAdminBuilder
parameter_list|(
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
name|conn
operator|.
name|getAdminBuilder
argument_list|(
name|pool
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncBufferedMutatorBuilder
name|getBufferedMutatorBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|conn
operator|.
name|getBufferedMutatorBuilder
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncBufferedMutatorBuilder
name|getBufferedMutatorBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
name|conn
operator|.
name|getBufferedMutatorBuilder
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Hbck
argument_list|>
name|getHbck
parameter_list|()
block|{
return|return
name|conn
operator|.
name|getHbck
argument_list|()
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
annotation|@
name|Override
specifier|public
name|Connection
name|toConnection
parameter_list|()
block|{
return|return
operator|new
name|SharedConnection
argument_list|(
name|conn
operator|.
name|toConnection
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

