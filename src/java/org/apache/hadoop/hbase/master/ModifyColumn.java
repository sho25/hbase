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
name|master
package|;
end_package

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
name|HColumnDescriptor
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
name|HRegionInterface
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/** Instantiated to modify an existing column family on a table */
end_comment

begin_class
class|class
name|ModifyColumn
extends|extends
name|ColumnOperation
block|{
specifier|private
specifier|final
name|HColumnDescriptor
name|descriptor
decl_stmt|;
specifier|private
specifier|final
name|Text
name|columnName
decl_stmt|;
name|ModifyColumn
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|Text
name|tableName
parameter_list|,
specifier|final
name|Text
name|columnName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|descriptor
operator|=
name|descriptor
expr_stmt|;
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|postProcessMeta
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HRegionInterface
name|server
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|HRegionInfo
name|i
range|:
name|unservedRegions
control|)
block|{
comment|// get the column families map from the table descriptor
name|Map
argument_list|<
name|Text
argument_list|,
name|HColumnDescriptor
argument_list|>
name|families
init|=
name|i
operator|.
name|getTableDesc
argument_list|()
operator|.
name|families
argument_list|()
decl_stmt|;
comment|// if the table already has this column, then put the new descriptor
comment|// version.
if|if
condition|(
name|families
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|families
operator|.
name|put
argument_list|(
name|columnName
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
name|updateRegionInfo
argument_list|(
name|server
argument_list|,
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// otherwise, we have an error.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Column family '"
operator|+
name|columnName
operator|+
literal|"' doesn't exist, so cannot be modified."
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

