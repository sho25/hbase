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
name|classification
operator|.
name|InterfaceStability
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
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|UnmodifyableHTableDescriptor
extends|extends
name|HTableDescriptor
block|{
comment|/** Default constructor */
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
name|getTableName
argument_list|()
argument_list|,
name|getUnmodifyableFamilies
argument_list|(
name|desc
argument_list|)
argument_list|,
name|desc
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * @param desc    * @return Families as unmodifiable array.    */
specifier|private
specifier|static
name|HColumnDescriptor
index|[]
name|getUnmodifyableFamilies
parameter_list|(
specifier|final
name|HTableDescriptor
name|desc
parameter_list|)
block|{
name|HColumnDescriptor
index|[]
name|f
init|=
operator|new
name|HColumnDescriptor
index|[
name|desc
operator|.
name|getFamilies
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
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
name|f
index|[
name|i
operator|++
index|]
operator|=
name|c
expr_stmt|;
block|}
return|return
name|f
return|;
block|}
comment|/**    * Does NOT add a column family. This object is immutable    * @param family HColumnDescriptor of familyto add.    */
annotation|@
name|Override
specifier|public
name|HTableDescriptor
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
comment|/**    * @see org.apache.hadoop.hbase.HTableDescriptor#setReadOnly(boolean)    */
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|setReadOnly
parameter_list|(
name|boolean
name|readOnly
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
comment|/**    * @see org.apache.hadoop.hbase.HTableDescriptor#setValue(byte[], byte[])    */
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|setValue
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
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
comment|/**    * @see org.apache.hadoop.hbase.HTableDescriptor#setValue(java.lang.String, java.lang.String)    */
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|setValue
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
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
comment|/**    * @see org.apache.hadoop.hbase.HTableDescriptor#setMaxFileSize(long)    */
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|setMaxFileSize
parameter_list|(
name|long
name|maxFileSize
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
comment|/**    * @see org.apache.hadoop.hbase.HTableDescriptor#setMemStoreFlushSize(long)    */
annotation|@
name|Override
specifier|public
name|HTableDescriptor
name|setMemStoreFlushSize
parameter_list|(
name|long
name|memstoreFlushSize
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
comment|//  /**
comment|//   * @see org.apache.hadoop.hbase.HTableDescriptor#addIndex(org.apache.hadoop.hbase.client.tableindexed.IndexSpecification)
comment|//   */
comment|//  @Override
comment|//  public void addIndex(IndexSpecification index) {
comment|//    throw new UnsupportedOperationException("HTableDescriptor is read-only");
comment|//  }
block|}
end_class

end_unit

