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
name|coprocessor
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
name|java
operator|.
name|util
operator|.
name|List
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
name|Mutation
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
name|ipc
operator|.
name|CoprocessorProtocol
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
name|regionserver
operator|.
name|HRegion
import|;
end_import

begin_comment
comment|/**  * Defines a protocol to perform multi row transactions.  * See {@link MultiRowMutationEndpoint} for the implementation.  *</br>  * See  * {@link HRegion#mutateRowsWithLocks(java.util.Collection, java.util.Collection)}  * for details and limitations.  *</br>  * Example:  *<code><pre>  * List<Mutation> mutations = ...;  * Put p1 = new Put(row1);  * Put p2 = new Put(row2);  * ...  * mutations.add(p1);  * mutations.add(p2);  * MultiRowMutationProtocol mrOp = t.coprocessorProxy(  *   MultiRowMutationProtocol.class, row1);  * mrOp.mutateRows(mutations);  *</pre></code>  */
end_comment

begin_interface
specifier|public
interface|interface
name|MultiRowMutationProtocol
extends|extends
name|CoprocessorProtocol
block|{
specifier|public
name|void
name|mutateRows
parameter_list|(
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutations
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

