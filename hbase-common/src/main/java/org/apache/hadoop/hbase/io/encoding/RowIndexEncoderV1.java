begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|io
operator|.
name|ByteArrayOutputStream
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
name|util
operator|.
name|Bytes
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RowIndexEncoderV1
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RowIndexEncoderV1
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The Cell previously appended. */
specifier|private
name|Cell
name|lastCell
init|=
literal|null
decl_stmt|;
specifier|private
name|DataOutputStream
name|out
decl_stmt|;
specifier|private
name|NoneEncoder
name|encoder
decl_stmt|;
specifier|private
name|int
name|startOffset
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|rowsOffsetBAOS
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|64
operator|*
literal|4
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HFileBlockEncodingContext
name|context
decl_stmt|;
specifier|public
name|RowIndexEncoderV1
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
name|encoder
operator|=
operator|new
name|NoneEncoder
argument_list|(
name|out
argument_list|,
name|encodingCtx
argument_list|)
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|encodingCtx
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
comment|// checkRow uses comparator to check we are writing in order.
name|int
name|extraBytesForRowIndex
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|!
name|checkRow
argument_list|(
name|cell
argument_list|)
condition|)
block|{
if|if
condition|(
name|startOffset
operator|<
literal|0
condition|)
block|{
name|startOffset
operator|=
name|out
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|rowsOffsetBAOS
operator|.
name|writeInt
argument_list|(
name|out
operator|.
name|size
argument_list|()
operator|-
name|startOffset
argument_list|)
expr_stmt|;
comment|// added for the int written in the previous line
name|extraBytesForRowIndex
operator|=
name|Bytes
operator|.
name|SIZEOF_INT
expr_stmt|;
block|}
name|lastCell
operator|=
name|cell
expr_stmt|;
name|int
name|size
init|=
name|encoder
operator|.
name|write
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|context
operator|.
name|getEncodingState
argument_list|()
operator|.
name|postCellEncode
argument_list|(
name|size
argument_list|,
name|size
operator|+
name|extraBytesForRowIndex
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|boolean
name|checkRow
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isDuplicateRow
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Key cannot be null or empty"
argument_list|)
throw|;
block|}
if|if
condition|(
name|lastCell
operator|!=
literal|null
condition|)
block|{
name|int
name|keyComp
init|=
name|this
operator|.
name|context
operator|.
name|getHFileContext
argument_list|()
operator|.
name|getCellComparator
argument_list|()
operator|.
name|compareRows
argument_list|(
name|lastCell
argument_list|,
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyComp
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Added a key not lexically larger than"
operator|+
literal|" previous. Current cell = "
operator|+
name|cell
operator|+
literal|", lastCell = "
operator|+
name|lastCell
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|keyComp
operator|==
literal|0
condition|)
block|{
name|isDuplicateRow
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|isDuplicateRow
return|;
block|}
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|onDiskDataSize
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|startOffset
operator|>=
literal|0
condition|)
block|{
name|onDiskDataSize
operator|=
name|out
operator|.
name|size
argument_list|()
operator|-
name|startOffset
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|rowsOffsetBAOS
operator|.
name|size
argument_list|()
operator|/
literal|4
argument_list|)
expr_stmt|;
if|if
condition|(
name|rowsOffsetBAOS
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|rowsOffsetBAOS
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|rowsOffsetBAOS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|onDiskDataSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"RowNumber: "
operator|+
name|rowsOffsetBAOS
operator|.
name|size
argument_list|()
operator|/
literal|4
operator|+
literal|", onDiskDataSize: "
operator|+
name|onDiskDataSize
operator|+
literal|", totalOnDiskSize: "
operator|+
operator|(
name|out
operator|.
name|size
argument_list|()
operator|-
name|startOffset
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|beforeShipped
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|lastCell
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|lastCell
operator|=
name|KeyValueUtil
operator|.
name|toNewKeyCell
argument_list|(
name|this
operator|.
name|lastCell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

