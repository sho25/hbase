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
name|client
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
comment|/**  * Read-only table descriptor.  * Returned out of {@link HTable.getTableDescriptor}.  */
end_comment

begin_class
specifier|public
class|class
name|UnmodifyableHTableDescriptor
extends|extends
name|HTableDescriptor
block|{
specifier|public
name|UnmodifyableHTableDescriptor
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/*    * Create an unmodifyable copy of an HTableDescriptor    * @param desc    */
name|UnmodifyableHTableDescriptor
parameter_list|(
specifier|final
name|HTableDescriptor
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|c
range|:
name|desc
operator|.
name|getFamilies
argument_list|()
control|)
block|{
name|super
operator|.
name|addFamily
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Does NOT add a column family. This object is immutable    * @param family HColumnDescriptor of familyto add.    */
annotation|@
name|Override
specifier|public
name|void
name|addFamily
parameter_list|(
specifier|final
name|HColumnDescriptor
name|family
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HTableDescriptor is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @param column    * @return Column descriptor for the passed family name or the family on    * passed in column.    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
name|removeFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
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

