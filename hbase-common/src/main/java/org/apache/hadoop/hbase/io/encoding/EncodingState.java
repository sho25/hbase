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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Keeps track of the encoding state.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|EncodingState
block|{
comment|/**    * The previous Cell the encoder encoded.    */
specifier|protected
name|Cell
name|prevCell
init|=
literal|null
decl_stmt|;
specifier|public
name|void
name|beforeShipped
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|prevCell
operator|!=
literal|null
condition|)
block|{
comment|// can't use KeyValueUtil#toNewKeyCell, because we need both key and value
comment|// from the prevCell in FastDiffDeltaEncoder
name|this
operator|.
name|prevCell
operator|=
name|KeyValueUtil
operator|.
name|copyToNewKeyValue
argument_list|(
name|this
operator|.
name|prevCell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

