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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * A custom RegionSplitPolicy implementing a SplitPolicy that groups  * rows by a prefix of the row-key  *  * This ensures that a region is not split "inside" a prefix of a row key.  * I.e. rows can be co-located in a regionb by their prefix.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|KeyPrefixRegionSplitPolicy
extends|extends
name|IncreasingToUpperBoundRegionSplitPolicy
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|KeyPrefixRegionSplitPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PREFIX_LENGTH_KEY
init|=
literal|"prefix_split_key_policy.prefix_length"
decl_stmt|;
specifier|private
name|int
name|prefixLength
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|configureForRegion
parameter_list|(
name|HRegion
name|region
parameter_list|)
block|{
name|super
operator|.
name|configureForRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|prefixLength
operator|=
literal|0
expr_stmt|;
comment|// read the prefix length from the table descriptor
name|String
name|prefixLengthString
init|=
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getValue
argument_list|(
name|PREFIX_LENGTH_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|prefixLengthString
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|PREFIX_LENGTH_KEY
operator|+
literal|" not specified for table "
operator|+
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|". Using default RegionSplitPolicy"
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|prefixLength
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|prefixLengthString
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
comment|// ignore
block|}
if|if
condition|(
name|prefixLength
operator|<=
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Invalid value for "
operator|+
name|PREFIX_LENGTH_KEY
operator|+
literal|" for table "
operator|+
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|":"
operator|+
name|prefixLengthString
operator|+
literal|". Using default RegionSplitPolicy"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|byte
index|[]
name|getSplitPoint
parameter_list|()
block|{
name|byte
index|[]
name|splitPoint
init|=
name|super
operator|.
name|getSplitPoint
argument_list|()
decl_stmt|;
if|if
condition|(
name|prefixLength
operator|>
literal|0
operator|&&
name|splitPoint
operator|!=
literal|null
operator|&&
name|splitPoint
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// group split keys by a prefix
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|splitPoint
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|prefixLength
argument_list|,
name|splitPoint
operator|.
name|length
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|splitPoint
return|;
block|}
block|}
block|}
end_class

end_unit

