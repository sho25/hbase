begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
package|;
end_package

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
name|coprocessor
operator|.
name|*
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

begin_comment
comment|/**  * Provides the coprocessor framework and environment for master oriented  * operations.  {@link HMaster} interacts with the loaded coprocessors  * through this class.  */
end_comment

begin_class
specifier|public
class|class
name|MasterCoprocessorHost
extends|extends
name|CoprocessorHost
argument_list|<
name|MasterCoprocessorHost
operator|.
name|MasterEnvironment
argument_list|>
block|{
comment|/**    * Coprocessor environment extension providing access to master related    * services.    */
specifier|static
class|class
name|MasterEnvironment
extends|extends
name|CoprocessorHost
operator|.
name|Environment
implements|implements
name|MasterCoprocessorEnvironment
block|{
specifier|private
name|MasterServices
name|masterServices
decl_stmt|;
specifier|public
name|MasterEnvironment
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
name|Coprocessor
name|impl
parameter_list|,
name|Coprocessor
operator|.
name|Priority
name|priority
parameter_list|,
name|MasterServices
name|services
parameter_list|)
block|{
name|super
argument_list|(
name|impl
argument_list|,
name|priority
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterServices
operator|=
name|services
expr_stmt|;
block|}
specifier|public
name|MasterServices
name|getMasterServices
parameter_list|()
block|{
return|return
name|masterServices
return|;
block|}
block|}
specifier|private
name|MasterServices
name|masterServices
decl_stmt|;
name|MasterCoprocessorHost
parameter_list|(
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|masterServices
operator|=
name|services
expr_stmt|;
name|loadSystemCoprocessors
argument_list|(
name|conf
argument_list|,
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MasterEnvironment
name|createEnvironment
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
name|Coprocessor
name|instance
parameter_list|,
name|Coprocessor
operator|.
name|Priority
name|priority
parameter_list|)
block|{
return|return
operator|new
name|MasterEnvironment
argument_list|(
name|implClass
argument_list|,
name|instance
argument_list|,
name|priority
argument_list|,
name|masterServices
argument_list|)
return|;
block|}
comment|/* Implementation of hooks for invoking MasterObservers */
name|void
name|preCreateTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preCreateTable
argument_list|(
name|env
argument_list|,
name|desc
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postCreateTable
parameter_list|(
name|HRegionInfo
index|[]
name|regions
parameter_list|,
name|boolean
name|sync
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postCreateTable
argument_list|(
name|env
argument_list|,
name|regions
argument_list|,
name|sync
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preDeleteTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preDeleteTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postDeleteTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postDeleteTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preModifyTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preModifyTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|htd
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postModifyTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postModifyTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|htd
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preAddColumn
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preAddColumn
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|column
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postAddColumn
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postAddColumn
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|column
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preModifyColumn
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preModifyColumn
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postModifyColumn
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postModifyColumn
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preDeleteColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preDeleteColumn
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|c
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postDeleteColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postDeleteColumn
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|c
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preEnableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preEnableTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postEnableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postEnableTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preDisableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preDisableTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postDisableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postDisableTable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preMove
parameter_list|(
specifier|final
name|HRegionInfo
name|region
parameter_list|,
specifier|final
name|HServerInfo
name|srcServer
parameter_list|,
specifier|final
name|HServerInfo
name|destServer
parameter_list|)
throws|throws
name|UnknownRegionException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preMove
argument_list|(
name|env
argument_list|,
name|region
argument_list|,
name|srcServer
argument_list|,
name|destServer
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|postMove
parameter_list|(
specifier|final
name|HRegionInfo
name|region
parameter_list|,
specifier|final
name|HServerInfo
name|srcServer
parameter_list|,
specifier|final
name|HServerInfo
name|destServer
parameter_list|)
throws|throws
name|UnknownRegionException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postMove
argument_list|(
name|env
argument_list|,
name|region
argument_list|,
name|srcServer
argument_list|,
name|destServer
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|boolean
name|preAssign
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|bypass
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preAssign
argument_list|(
name|env
argument_list|,
name|regionName
argument_list|,
name|force
argument_list|)
expr_stmt|;
name|bypass
operator||=
name|env
operator|.
name|shouldBypass
argument_list|()
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
return|return
name|bypass
return|;
block|}
name|void
name|postAssign
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postAssign
argument_list|(
name|env
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|boolean
name|preUnassign
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|bypass
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preUnassign
argument_list|(
name|env
argument_list|,
name|regionName
argument_list|,
name|force
argument_list|)
expr_stmt|;
name|bypass
operator||=
name|env
operator|.
name|shouldBypass
argument_list|()
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
return|return
name|bypass
return|;
block|}
name|void
name|postUnassign
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postUnassign
argument_list|(
name|env
argument_list|,
name|regionInfo
argument_list|,
name|force
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|boolean
name|preBalance
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|bypass
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preBalance
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|bypass
operator||=
name|env
operator|.
name|shouldBypass
argument_list|()
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
return|return
name|bypass
return|;
block|}
name|void
name|postBalance
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postBalance
argument_list|(
name|env
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|boolean
name|preBalanceSwitch
parameter_list|(
specifier|final
name|boolean
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|balance
init|=
name|b
decl_stmt|;
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
name|balance
operator|=
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preBalanceSwitch
argument_list|(
name|env
argument_list|,
name|balance
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
return|return
name|balance
return|;
block|}
name|void
name|postBalanceSwitch
parameter_list|(
specifier|final
name|boolean
name|oldValue
parameter_list|,
specifier|final
name|boolean
name|newValue
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|postBalanceSwitch
argument_list|(
name|env
argument_list|,
name|oldValue
argument_list|,
name|newValue
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preShutdown
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preShutdown
argument_list|(
name|env
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
name|void
name|preStopMaster
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|MasterEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|MasterObserver
condition|)
block|{
operator|(
operator|(
name|MasterObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preStopMaster
argument_list|(
name|env
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

