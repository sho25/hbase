begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
package|;
end_package

begin_comment
comment|/**  * This interface is used for the tracking and enforcement of Deletes  * during the course of a Get or Scan operation.  *<p>  * This class is utilized through three methods:  *<ul><li>{@link #add} when encountering a Delete  *<li>{@link #isDeleted} when checking if a Put KeyValue has been deleted  *<li>{@link #update} when reaching the end of a StoreFile  */
end_comment

begin_interface
specifier|public
interface|interface
name|DeleteTracker
block|{
comment|/**    * Add the specified KeyValue to the list of deletes to check against for    * this row operation.    *<p>    * This is called when a Delete is encountered in a StoreFile.    * @param buffer KeyValue buffer    * @param qualifierOffset column qualifier offset    * @param qualifierLength column qualifier length    * @param timestamp timestamp    * @param type delete type as byte    */
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|qualifierOffset
parameter_list|,
name|int
name|qualifierLength
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
name|type
parameter_list|)
function_decl|;
comment|/**    * Check if the specified KeyValue buffer has been deleted by a previously    * seen delete.    * @param buffer KeyValue buffer    * @param qualifierOffset column qualifier offset    * @param qualifierLength column qualifier length    * @param timestamp timestamp    * @return true is the specified KeyValue is deleted, false if not    */
specifier|public
name|boolean
name|isDeleted
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|qualifierOffset
parameter_list|,
name|int
name|qualifierLength
parameter_list|,
name|long
name|timestamp
parameter_list|)
function_decl|;
comment|/**    * @return true if there are no current delete, false otherwise    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
function_decl|;
comment|/**    * Called at the end of every StoreFile.    *<p>    * Many optimized implementations of Trackers will require an update at    * when the end of each StoreFile is reached.    */
specifier|public
name|void
name|update
parameter_list|()
function_decl|;
comment|/**    * Called between rows.    *<p>    * This clears everything as if a new DeleteTracker was instantiated.    */
specifier|public
name|void
name|reset
parameter_list|()
function_decl|;
comment|/**    * Return codes for comparison of two Deletes.    *<p>    * The codes tell the merging function what to do.    *<p>    * INCLUDE means add the specified Delete to the merged list.    * NEXT means move to the next element in the specified list(s).    */
enum|enum
name|DeleteCompare
block|{
name|INCLUDE_OLD_NEXT_OLD
block|,
name|INCLUDE_OLD_NEXT_BOTH
block|,
name|INCLUDE_NEW_NEXT_NEW
block|,
name|INCLUDE_NEW_NEXT_BOTH
block|,
name|NEXT_OLD
block|,
name|NEXT_NEW
block|}
block|}
end_interface

end_unit

