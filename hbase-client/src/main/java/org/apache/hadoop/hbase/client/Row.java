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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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

begin_comment
comment|/**  * Has a row.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|Row
extends|extends
name|Comparable
argument_list|<
name|Row
argument_list|>
block|{
name|Comparator
argument_list|<
name|Row
argument_list|>
name|COMPARATOR
init|=
parameter_list|(
name|v1
parameter_list|,
name|v2
parameter_list|)
lambda|->
name|Bytes
operator|.
name|compareTo
argument_list|(
name|v1
operator|.
name|getRow
argument_list|()
argument_list|,
name|v2
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * @return The row.    */
name|byte
index|[]
name|getRow
parameter_list|()
function_decl|;
comment|/**    * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0.    *             Use {@link Row#COMPARATOR} instead    */
annotation|@
name|Deprecated
name|int
name|compareTo
parameter_list|(
name|Row
name|var1
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

