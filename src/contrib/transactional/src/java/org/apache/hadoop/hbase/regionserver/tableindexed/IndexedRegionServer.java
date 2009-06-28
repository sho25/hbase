begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|tableindexed
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseConfiguration
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|ipc
operator|.
name|HBaseRPCProtocolVersion
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
name|ipc
operator|.
name|IndexedRegionInterface
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|transactional
operator|.
name|TransactionalRegionServer
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
name|Progressable
import|;
end_import

begin_comment
comment|/**  * RegionServer which maintains secondary indexes.  *   **/
end_comment

begin_class
specifier|public
class|class
name|IndexedRegionServer
extends|extends
name|TransactionalRegionServer
implements|implements
name|IndexedRegionInterface
block|{
specifier|public
name|IndexedRegionServer
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
specifier|final
name|String
name|protocol
parameter_list|,
specifier|final
name|long
name|clientVersion
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|protocol
operator|.
name|equals
argument_list|(
name|IndexedRegionInterface
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|HBaseRPCProtocolVersion
operator|.
name|versionID
return|;
block|}
return|return
name|super
operator|.
name|getProtocolVersion
argument_list|(
name|protocol
argument_list|,
name|clientVersion
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HRegion
name|instantiateRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|r
init|=
operator|new
name|IndexedRegion
argument_list|(
name|HTableDescriptor
operator|.
name|getTableDir
argument_list|(
name|super
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|regionInfo
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|super
operator|.
name|hlog
argument_list|,
name|super
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|super
operator|.
name|conf
argument_list|,
name|regionInfo
argument_list|,
name|super
operator|.
name|getFlushRequester
argument_list|()
argument_list|,
name|super
operator|.
name|getTransactionalLeases
argument_list|()
argument_list|)
decl_stmt|;
name|r
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
operator|new
name|Progressable
argument_list|()
block|{
specifier|public
name|void
name|progress
parameter_list|()
block|{
name|addProcessingMessage
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
block|}
end_class

end_unit

