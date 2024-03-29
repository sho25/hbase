begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|encoding
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|Cell
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
name|KeyValue
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
name|KeyValueUtil
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
name|PrivateCellUtil
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
name|WritableUtils
import|;
end_import

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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NoneEncoder
block|{
specifier|private
name|DataOutputStream
name|out
decl_stmt|;
specifier|private
name|HFileBlockDefaultEncodingContext
name|encodingCtx
decl_stmt|;
specifier|public
name|NoneEncoder
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|HFileBlockDefaultEncodingContext
name|encodingCtx
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|this
operator|.
name|encodingCtx
operator|=
name|encodingCtx
expr_stmt|;
block|}
specifier|public
name|int
name|write
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
comment|// We write tags seperately because though there is no tag in KV
comment|// if the hfilecontext says include tags we need the tags length to be
comment|// written
name|int
name|size
init|=
name|KeyValueUtil
operator|.
name|oswrite
argument_list|(
name|cell
argument_list|,
name|out
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Write the additional tag into the stream
if|if
condition|(
name|encodingCtx
operator|.
name|getHFileContext
argument_list|()
operator|.
name|isIncludesTags
argument_list|()
condition|)
block|{
name|int
name|tagsLength
init|=
name|cell
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeShort
argument_list|(
name|tagsLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|tagsLength
operator|>
literal|0
condition|)
block|{
name|PrivateCellUtil
operator|.
name|writeTags
argument_list|(
name|out
argument_list|,
name|cell
argument_list|,
name|tagsLength
argument_list|)
expr_stmt|;
block|}
name|size
operator|+=
name|tagsLength
operator|+
name|KeyValue
operator|.
name|TAGS_LENGTH_SIZE
expr_stmt|;
block|}
if|if
condition|(
name|encodingCtx
operator|.
name|getHFileContext
argument_list|()
operator|.
name|isIncludesMvcc
argument_list|()
condition|)
block|{
name|WritableUtils
operator|.
name|writeVLong
argument_list|(
name|out
argument_list|,
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|+=
name|WritableUtils
operator|.
name|getVIntSize
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
block|}
end_class

end_unit

