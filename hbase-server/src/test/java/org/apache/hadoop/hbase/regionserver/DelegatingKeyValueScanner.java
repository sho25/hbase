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
name|fs
operator|.
name|Path
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
name|client
operator|.
name|Scan
import|;
end_import

begin_class
specifier|public
class|class
name|DelegatingKeyValueScanner
implements|implements
name|KeyValueScanner
block|{
specifier|protected
name|KeyValueScanner
name|delegate
decl_stmt|;
specifier|public
name|DelegatingKeyValueScanner
parameter_list|(
name|KeyValueScanner
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shipped
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|shipped
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|peek
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|peek
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|next
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|seek
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reseek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|reseek
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getScannerOrder
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getScannerOrder
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldUseScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|HStore
name|store
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
return|return
name|delegate
operator|.
name|shouldUseScanner
argument_list|(
name|scan
argument_list|,
name|store
argument_list|,
name|oldestUnexpiredTS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|requestSeek
parameter_list|(
name|Cell
name|kv
parameter_list|,
name|boolean
name|forward
parameter_list|,
name|boolean
name|useBloom
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|requestSeek
argument_list|(
name|kv
argument_list|,
name|forward
argument_list|,
name|useBloom
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|realSeekDone
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|realSeekDone
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|enforceSeek
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|enforceSeek
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFileScanner
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|isFileScanner
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getFilePath
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getFilePath
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|backwardSeek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|backwardSeek
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekToPreviousRow
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|seekToPreviousRow
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekToLastRow
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|seekToLastRow
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextIndexedKey
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getNextIndexedKey
argument_list|()
return|;
block|}
block|}
end_class

end_unit

