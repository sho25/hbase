begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|io
operator|.
name|Text
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * Batch update operations such as put, delete, and deleteAll.  */
end_comment

begin_class
specifier|public
class|class
name|BatchOperation
implements|implements
name|Writable
block|{
comment|/**     * Operation types.    * @see org.apache.hadoop.io.SequenceFile.Writer    */
specifier|public
specifier|static
enum|enum
name|Operation
block|{
comment|/** update a field */
name|PUT
block|,
comment|/** delete a field */
name|DELETE
block|}
specifier|private
name|Operation
name|op
decl_stmt|;
specifier|private
name|Text
name|column
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
comment|/** default constructor used by Writable */
specifier|public
name|BatchOperation
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|Text
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a DELETE operation    *     * @param column column name    */
specifier|public
name|BatchOperation
parameter_list|(
specifier|final
name|Text
name|column
parameter_list|)
block|{
name|this
argument_list|(
name|Operation
operator|.
name|DELETE
argument_list|,
name|column
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a PUT operation    *     * @param column column name    * @param value column value    */
specifier|public
name|BatchOperation
parameter_list|(
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|Operation
operator|.
name|PUT
argument_list|,
name|column
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a put operation    *    * @param operation the operation (put or get)    * @param column column name    * @param value column value    */
specifier|public
name|BatchOperation
parameter_list|(
specifier|final
name|Operation
name|operation
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|operation
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * @return the column    */
specifier|public
name|Text
name|getColumn
parameter_list|()
block|{
return|return
name|column
return|;
block|}
comment|/**    * @return the operation    */
specifier|public
name|Operation
name|getOp
parameter_list|()
block|{
return|return
name|this
operator|.
name|op
return|;
block|}
comment|/**    * @return the value    */
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|//
comment|// Writable
comment|//
comment|/**    * {@inheritDoc}    */
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ordinal
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|op
operator|=
name|Operation
operator|.
name|values
argument_list|()
index|[
name|ordinal
index|]
expr_stmt|;
name|column
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|op
operator|==
name|Operation
operator|.
name|PUT
condition|)
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * {@inheritDoc}    */
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|op
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|column
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|op
operator|==
name|Operation
operator|.
name|PUT
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

