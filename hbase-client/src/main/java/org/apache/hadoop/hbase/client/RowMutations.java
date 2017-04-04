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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Performs multiple mutations atomically on a single row.  * Currently {@link Put} and {@link Delete} are supported.  *  * The mutations are performed in the order in which they  * were added.  *   *<p>We compare and equate mutations based off their row so be careful putting RowMutations  * into Sets or using them as keys in Maps.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|RowMutations
implements|implements
name|Row
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutations
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|public
name|RowMutations
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an atomic mutation for the specified row.    * @param row row key    * @param initialCapacity the initial capacity of the RowMutations    */
specifier|public
name|RowMutations
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|initialCapacity
parameter_list|)
block|{
name|Mutation
operator|.
name|checkRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|Bytes
operator|.
name|copy
argument_list|(
name|row
argument_list|)
expr_stmt|;
if|if
condition|(
name|initialCapacity
operator|<=
literal|0
condition|)
block|{
name|this
operator|.
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|mutations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|initialCapacity
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add a {@link Put} operation to the list of mutations    * @param p The {@link Put} to add    * @throws IOException    */
specifier|public
name|void
name|add
parameter_list|(
name|Put
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|internalAdd
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a {@link Delete} operation to the list of mutations    * @param d The {@link Delete} to add    * @throws IOException    */
specifier|public
name|void
name|add
parameter_list|(
name|Delete
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|internalAdd
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|internalAdd
parameter_list|(
name|Mutation
name|m
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|res
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|WrongRowIOException
argument_list|(
literal|"The row in the recently added Put/Delete<"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"> doesn't match the original one<"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|row
argument_list|)
operator|+
literal|">"
argument_list|)
throw|;
block|}
name|mutations
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Row
name|i
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|getRow
argument_list|()
argument_list|,
name|i
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|instanceof
name|RowMutations
condition|)
block|{
name|RowMutations
name|other
init|=
operator|(
name|RowMutations
operator|)
name|obj
decl_stmt|;
return|return
name|compareTo
argument_list|(
name|other
argument_list|)
operator|==
literal|0
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|hashCode
argument_list|(
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
comment|/**    * @return An unmodifiable list of the current mutations.    */
specifier|public
name|List
argument_list|<
name|Mutation
argument_list|>
name|getMutations
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|mutations
argument_list|)
return|;
block|}
block|}
end_class

end_unit

