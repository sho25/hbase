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
operator|.
name|wal
package|;
end_package

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
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|TagCompressionContext
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
name|util
operator|.
name|Dictionary
import|;
end_import

begin_comment
comment|/**  * Context that holds the various dictionaries for compression in HLog.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|CompressionContext
block|{
specifier|static
specifier|final
name|String
name|ENABLE_WAL_TAGS_COMPRESSION
init|=
literal|"hbase.regionserver.wal.tags.enablecompression"
decl_stmt|;
specifier|final
name|Dictionary
name|regionDict
decl_stmt|;
specifier|final
name|Dictionary
name|tableDict
decl_stmt|;
specifier|final
name|Dictionary
name|familyDict
decl_stmt|;
specifier|final
name|Dictionary
name|qualifierDict
decl_stmt|;
specifier|final
name|Dictionary
name|rowDict
decl_stmt|;
comment|// Context used for compressing tags
name|TagCompressionContext
name|tagCompressionContext
init|=
literal|null
decl_stmt|;
specifier|public
name|CompressionContext
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Dictionary
argument_list|>
name|dictType
parameter_list|,
name|boolean
name|recoveredEdits
parameter_list|,
name|boolean
name|hasTagCompression
parameter_list|)
throws|throws
name|SecurityException
throws|,
name|NoSuchMethodException
throws|,
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|InvocationTargetException
block|{
name|Constructor
argument_list|<
name|?
extends|extends
name|Dictionary
argument_list|>
name|dictConstructor
init|=
name|dictType
operator|.
name|getConstructor
argument_list|()
decl_stmt|;
name|regionDict
operator|=
name|dictConstructor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|tableDict
operator|=
name|dictConstructor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|familyDict
operator|=
name|dictConstructor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|qualifierDict
operator|=
name|dictConstructor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|rowDict
operator|=
name|dictConstructor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
if|if
condition|(
name|recoveredEdits
condition|)
block|{
comment|// This will never change
name|regionDict
operator|.
name|init
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|tableDict
operator|.
name|init
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionDict
operator|.
name|init
argument_list|(
name|Short
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|tableDict
operator|.
name|init
argument_list|(
name|Short
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
name|rowDict
operator|.
name|init
argument_list|(
name|Short
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|familyDict
operator|.
name|init
argument_list|(
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|qualifierDict
operator|.
name|init
argument_list|(
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasTagCompression
condition|)
block|{
name|tagCompressionContext
operator|=
operator|new
name|TagCompressionContext
argument_list|(
name|dictType
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|clear
parameter_list|()
block|{
name|regionDict
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tableDict
operator|.
name|clear
argument_list|()
expr_stmt|;
name|familyDict
operator|.
name|clear
argument_list|()
expr_stmt|;
name|qualifierDict
operator|.
name|clear
argument_list|()
expr_stmt|;
name|rowDict
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|tagCompressionContext
operator|!=
literal|null
condition|)
block|{
name|tagCompressionContext
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

