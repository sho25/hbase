begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
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
name|util
operator|.
name|Bytes
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
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_comment
comment|/**  * A Filter that stops after the given row.  There is no "RowStopFilter" because  * the Scan spec allows you to specify a stop row.  *  * Use this filter to include the stop row, eg: [A,Z].  */
end_comment

begin_class
specifier|public
class|class
name|InclusiveStopFilter
implements|implements
name|Filter
block|{
specifier|private
name|byte
index|[]
name|stopRowKey
decl_stmt|;
specifier|private
name|boolean
name|done
init|=
literal|false
decl_stmt|;
specifier|public
name|InclusiveStopFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|InclusiveStopFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|stopRowKey
parameter_list|)
block|{
name|this
operator|.
name|stopRowKey
operator|=
name|stopRowKey
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
comment|// noop, no state
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|stopRowKey
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
comment|//filter...
block|}
return|return
literal|false
return|;
block|}
comment|// if stopRowKey is<= buffer, then true, filter row.
name|int
name|cmp
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|stopRowKey
argument_list|,
literal|0
argument_list|,
name|stopRowKey
operator|.
name|length
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
block|{
name|done
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|done
return|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
name|done
return|;
block|}
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
comment|// include everything.
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
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
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|stopRowKey
argument_list|)
expr_stmt|;
block|}
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
name|this
operator|.
name|stopRowKey
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

