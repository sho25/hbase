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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|Checksum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_comment
comment|/**  * Checksum types. The Checksum type is a one byte number  * that stores a representation of the checksum algorithm  * used to encode a hfile. The ordinal of these cannot   * change or else you risk breaking all existing HFiles out there.  */
end_comment

begin_enum
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
name|void
name|initialize
parameter_list|()
block|{
comment|// do nothing
block|}
annotation|@
name|Override
specifier|public
name|Checksum
name|getChecksumObject
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
comment|// checksums not used
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
specifier|private
specifier|transient
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
decl_stmt|;
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
name|void
name|initialize
parameter_list|()
block|{
specifier|final
name|String
name|PURECRC32
init|=
literal|"org.apache.hadoop.util.PureJavaCrc32"
decl_stmt|;
specifier|final
name|String
name|JDKCRC
init|=
literal|"java.util.zip.CRC32"
decl_stmt|;
name|LOG
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ChecksumType
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// check if hadoop library is available
try|try
block|{
name|ctor
operator|=
name|ChecksumFactory
operator|.
name|newConstructor
argument_list|(
name|PURECRC32
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|PURECRC32
operator|+
literal|" available"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|PURECRC32
operator|+
literal|" not available."
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// The default checksum class name is java.util.zip.CRC32.
comment|// This is available on all JVMs.
if|if
condition|(
name|ctor
operator|==
literal|null
condition|)
block|{
name|ctor
operator|=
name|ChecksumFactory
operator|.
name|newConstructor
argument_list|(
name|JDKCRC
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|JDKCRC
operator|+
literal|" available"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|JDKCRC
operator|+
literal|" not available."
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Checksum
name|getChecksumObject
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|ctor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Bad constructor for "
operator|+
name|getName
argument_list|()
argument_list|)
throw|;
block|}
try|try
block|{
return|return
operator|(
name|Checksum
operator|)
name|ctor
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
specifier|private
specifier|transient
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
decl_stmt|;
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
name|void
name|initialize
parameter_list|()
block|{
specifier|final
name|String
name|PURECRC32C
init|=
literal|"org.apache.hadoop.util.PureJavaCrc32C"
decl_stmt|;
name|LOG
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ChecksumType
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|ctor
operator|=
name|ChecksumFactory
operator|.
name|newConstructor
argument_list|(
name|PURECRC32C
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|PURECRC32C
operator|+
literal|" available"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|PURECRC32C
operator|+
literal|" not available."
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Checksum
name|getChecksumObject
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|ctor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Bad constructor for "
operator|+
name|getName
argument_list|()
argument_list|)
throw|;
block|}
try|try
block|{
return|return
operator|(
name|Checksum
operator|)
name|ctor
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|;
specifier|private
specifier|final
name|byte
name|code
decl_stmt|;
specifier|protected
name|Log
name|LOG
decl_stmt|;
comment|/** initializes the relevant checksum class object */
specifier|abstract
name|void
name|initialize
parameter_list|()
function_decl|;
comment|/** returns the name of this checksum type */
specifier|public
specifier|abstract
name|String
name|getName
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
name|initialize
argument_list|()
expr_stmt|;
block|}
comment|/** returns a object that can be used to generate/validate checksums */
specifier|public
specifier|abstract
name|Checksum
name|getChecksumObject
parameter_list|()
throws|throws
name|IOException
function_decl|;
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

