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
name|client
package|;
end_package

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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
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
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
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
name|HTableDescriptor
import|;
end_import

begin_comment
comment|/**  * Read-only table descriptor.  */
end_comment

begin_class
annotation|@
name|Deprecated
comment|// deprecated for hbase 2.0, remove for hbase 3.0. see HTableDescriptor.
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ImmutableHTableDescriptor
extends|extends
name|HTableDescriptor
block|{
annotation|@
name|Override
specifier|protected
name|HColumnDescriptor
name|toHColumnDescriptor
parameter_list|(
name|ColumnFamilyDescriptor
name|desc
parameter_list|)
block|{
if|if
condition|(
name|desc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|desc
operator|instanceof
name|HColumnDescriptor
condition|)
block|{
return|return
operator|new
name|ImmutableHColumnDescriptor
argument_list|(
operator|(
name|HColumnDescriptor
operator|)
name|desc
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ImmutableHColumnDescriptor
argument_list|(
name|desc
argument_list|)
return|;
block|}
block|}
comment|/*    * Create an unmodifyable copy of an HTableDescriptor    * @param desc    */
specifier|public
name|ImmutableHTableDescriptor
parameter_list|(
specifier|final
name|HTableDescriptor
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|desc
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ImmutableHTableDescriptor
parameter_list|(
specifier|final
name|TableDescriptor
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|desc
operator|instanceof
name|ModifyableTableDescriptor
condition|?
operator|(
name|ModifyableTableDescriptor
operator|)
name|desc
else|:
operator|new
name|ModifyableTableDescriptor
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|desc
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|ModifyableTableDescriptor
name|getDelegateeForModification
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HTableDescriptor is read-only"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

