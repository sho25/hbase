begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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
name|util
operator|.
name|DataChecksum
import|;
end_import

begin_comment
comment|/**  * Checksum types. The Checksum type is a one byte number  * that stores a representation of the checksum algorithm  * used to encode a hfile. The ordinal of these cannot  * change or else you risk breaking all existing HFiles out there.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|ChecksumType
block|{
name|NULL
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"NULL"
return|;
block|}
annotation|@
name|Override
specifier|public
name|DataChecksum
operator|.
name|Type
name|getDataChecksumType
parameter_list|()
block|{
return|return
name|DataChecksum
operator|.
name|Type
operator|.
name|NULL
return|;
block|}
block|}
block|,
name|CRC32
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"CRC32"
return|;
block|}
annotation|@
name|Override
specifier|public
name|DataChecksum
operator|.
name|Type
name|getDataChecksumType
parameter_list|()
block|{
return|return
name|DataChecksum
operator|.
name|Type
operator|.
name|CRC32
return|;
block|}
block|}
block|,
name|CRC32C
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"CRC32C"
return|;
block|}
annotation|@
name|Override
specifier|public
name|DataChecksum
operator|.
name|Type
name|getDataChecksumType
parameter_list|()
block|{
return|return
name|DataChecksum
operator|.
name|Type
operator|.
name|CRC32C
return|;
block|}
block|}
block|;
specifier|private
specifier|final
name|byte
name|code
decl_stmt|;
specifier|public
specifier|static
name|ChecksumType
name|getDefaultChecksumType
parameter_list|()
block|{
return|return
name|ChecksumType
operator|.
name|CRC32C
return|;
block|}
comment|/** returns the name of this checksum type */
specifier|public
specifier|abstract
name|String
name|getName
parameter_list|()
function_decl|;
comment|/** Function to get corresponding {@link org.apache.hadoop.util.DataChecksum.Type}. */
specifier|public
specifier|abstract
name|DataChecksum
operator|.
name|Type
name|getDataChecksumType
parameter_list|()
function_decl|;
specifier|private
name|ChecksumType
parameter_list|(
specifier|final
name|byte
name|c
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|c
expr_stmt|;
block|}
specifier|public
name|byte
name|getCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|code
return|;
block|}
comment|/**    * Cannot rely on enum ordinals . They change if item is removed or moved.    * Do our own codes.    * @param b    * @return Type associated with passed code.    */
specifier|public
specifier|static
name|ChecksumType
name|codeToType
parameter_list|(
specifier|final
name|byte
name|b
parameter_list|)
block|{
for|for
control|(
name|ChecksumType
name|t
range|:
name|ChecksumType
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|t
operator|.
name|getCode
argument_list|()
operator|==
name|b
condition|)
block|{
return|return
name|t
return|;
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown checksum type code "
operator|+
name|b
argument_list|)
throw|;
block|}
comment|/**    * Map a checksum name to a specific type.    * Do our own names.    * @param name    * @return Type associated with passed code.    */
specifier|public
specifier|static
name|ChecksumType
name|nameToType
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|ChecksumType
name|t
range|:
name|ChecksumType
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|t
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|t
return|;
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown checksum type name "
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
end_enum

end_unit

