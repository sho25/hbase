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
name|constraint
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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
name|HTableDescriptor
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
name|Put
import|;
end_import

begin_comment
comment|/**  * Apply a {@link Constraint} (in traditional database terminology) to a HTable.  * Any number of {@link Constraint Constraints} can be added to the table, in  * any order.  *<p>  * A {@link Constraint} must be added to a table before the table is loaded via  * {@link Constraints#add(HTableDescriptor, Class...)} or  * {@link Constraints#add(HTableDescriptor,  * org.apache.hadoop.hbase.util.Pair...)}  * (if you want to add a configuration with the {@link Constraint}). Constraints  * will be run in the order that they are added. Further, a Constraint will be  * configured before it is run (on load).  *<p>  * See {@link Constraints#enableConstraint(HTableDescriptor, Class)} and  * {@link Constraints#disableConstraint(HTableDescriptor, Class)} for  * enabling/disabling of a given {@link Constraint} after it has been added.  *<p>  * If a {@link Put} is invalid, the Constraint should throw some sort of  * {@link org.apache.hadoop.hbase.constraint.ConstraintException}, indicating  * that the {@link Put} has failed. When  * this exception is thrown, not further retries of the {@link Put} are  * attempted nor are any other {@link Constraint Constraints} attempted (the  * {@link Put} is clearly not valid). Therefore, there are performance  * implications in the order in which {@link BaseConstraint Constraints} are  * specified.  *<p>  * If a {@link Constraint} fails to fail the {@link Put} via a  * {@link org.apache.hadoop.hbase.constraint.ConstraintException}, but instead  * throws a {@link RuntimeException},  * the entire constraint processing mechanism ({@link ConstraintProcessor}) will  * be unloaded from the table. This ensures that the region server is still  * functional, but no more {@link Put Puts} will be checked via  * {@link Constraint Constraints}.  *<p>  * Further, {@link Constraint Constraints} should probably not be used to  * enforce cross-table references as it will cause tremendous write slowdowns,  * but it is possible.  *<p>  * NOTE: Implementing classes must have a nullary (no-args) constructor  *  * @see BaseConstraint  * @see Constraints  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Constraint
extends|extends
name|Configurable
block|{
comment|/**    * Check a {@link Put} to ensure it is valid for the table. If the {@link Put}    * is valid, then just return from the method. Otherwise, throw an    * {@link Exception} specifying what happened. This {@link Exception} is    * propagated back to the client so you can see what caused the {@link Put} to    * fail.    * @param p {@link Put} to check    * @throws org.apache.hadoop.hbase.constraint.ConstraintException when the    * {@link Put} does not match the    *         constraint.    */
name|void
name|check
parameter_list|(
name|Put
name|p
parameter_list|)
throws|throws
name|ConstraintException
function_decl|;
block|}
end_interface

end_unit

