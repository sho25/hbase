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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|other
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Detect if every KV has the same KeyValue.Type, in which case we don't need to store it for each  * KV.  If(allSameType) during conversion to byte[], then we can store the "onlyType" in blockMeta,  * therefore not repeating it for each cell and saving 1 byte per cell.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CellTypeEncoder
block|{
comment|/************* fields *********************/
specifier|protected
name|boolean
name|pendingFirstType
init|=
literal|true
decl_stmt|;
specifier|protected
name|boolean
name|allSameType
init|=
literal|true
decl_stmt|;
specifier|protected
name|byte
name|onlyType
decl_stmt|;
comment|/************* construct *********************/
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|pendingFirstType
operator|=
literal|true
expr_stmt|;
name|allSameType
operator|=
literal|true
expr_stmt|;
block|}
comment|/************* methods *************************/
specifier|public
name|void
name|add
parameter_list|(
name|byte
name|type
parameter_list|)
block|{
if|if
condition|(
name|pendingFirstType
condition|)
block|{
name|onlyType
operator|=
name|type
expr_stmt|;
name|pendingFirstType
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|onlyType
operator|!=
name|type
condition|)
block|{
name|allSameType
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**************** get/set **************************/
specifier|public
name|boolean
name|areAllSameType
parameter_list|()
block|{
return|return
name|allSameType
return|;
block|}
specifier|public
name|byte
name|getOnlyType
parameter_list|()
block|{
return|return
name|onlyType
return|;
block|}
block|}
end_class

end_unit

