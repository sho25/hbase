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
name|client
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
name|exceptions
operator|.
name|DeserializationException
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
name|filter
operator|.
name|FilterBase
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ColumnCountOnRowFilter
extends|extends
name|FilterBase
block|{
specifier|private
specifier|final
name|int
name|limit
decl_stmt|;
specifier|private
name|int
name|count
init|=
literal|0
decl_stmt|;
specifier|public
name|ColumnCountOnRowFilter
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|count
operator|++
expr_stmt|;
return|return
name|count
operator|>
name|limit
condition|?
name|ReturnCode
operator|.
name|NEXT_ROW
else|:
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|count
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|limit
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ColumnCountOnRowFilter
name|parseFrom
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
return|return
operator|new
name|ColumnCountOnRowFilter
argument_list|(
name|Bytes
operator|.
name|toInt
argument_list|(
name|bytes
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit
