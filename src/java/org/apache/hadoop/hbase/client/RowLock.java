begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Holds row name and lock id.  */
end_comment

begin_class
specifier|public
class|class
name|RowLock
block|{
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|lockId
init|=
operator|-
literal|1L
decl_stmt|;
comment|/**    * Creates a RowLock from a row and lock id    * @param row    * @param lockId    */
specifier|public
name|RowLock
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|long
name|lockId
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|lockId
operator|=
name|lockId
expr_stmt|;
block|}
comment|/**    * Creates a RowLock with only a lock id    * @param lockId    */
specifier|public
name|RowLock
parameter_list|(
specifier|final
name|long
name|lockId
parameter_list|)
block|{
name|this
operator|.
name|lockId
operator|=
name|lockId
expr_stmt|;
block|}
comment|/**    * Get the row for this RowLock    * @return the row    */
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
comment|/**    * Get the lock id from this RowLock    * @return the lock id    */
specifier|public
name|long
name|getLockId
parameter_list|()
block|{
return|return
name|lockId
return|;
block|}
block|}
end_class

end_unit

