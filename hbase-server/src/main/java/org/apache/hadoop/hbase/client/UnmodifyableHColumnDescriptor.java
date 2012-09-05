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
name|io
operator|.
name|hfile
operator|.
name|Compression
import|;
end_import

begin_comment
comment|/**  * Immutable HColumnDescriptor  */
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
name|UnmodifyableHColumnDescriptor
extends|extends
name|HColumnDescriptor
block|{
comment|/**    * @param desc wrapped    */
specifier|public
name|UnmodifyableHColumnDescriptor
parameter_list|(
specifier|final
name|HColumnDescriptor
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|desc
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.HColumnDescriptor#setValue(byte[], byte[])    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
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
literal|"HColumnDescriptor is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.HColumnDescriptor#setValue(java.lang.String, java.lang.String)    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
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
literal|"HColumnDescriptor is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.HColumnDescriptor#setMaxVersions(int)    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
name|setMaxVersions
parameter_list|(
name|int
name|maxVersions
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HColumnDescriptor is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.HColumnDescriptor#setInMemory(boolean)    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
name|setInMemory
parameter_list|(
name|boolean
name|inMemory
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HColumnDescriptor is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.HColumnDescriptor#setBlockCacheEnabled(boolean)    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
name|setBlockCacheEnabled
parameter_list|(
name|boolean
name|blockCacheEnabled
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HColumnDescriptor is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.HColumnDescriptor#setTimeToLive(int)    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
name|setTimeToLive
parameter_list|(
name|int
name|timeToLive
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HColumnDescriptor is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @see org.apache.hadoop.hbase.HColumnDescriptor#setCompressionType(org.apache.hadoop.hbase.io.hfile.Compression.Algorithm)    */
annotation|@
name|Override
specifier|public
name|HColumnDescriptor
name|setCompressionType
parameter_list|(
name|Compression
operator|.
name|Algorithm
name|type
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HColumnDescriptor is read-only"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

