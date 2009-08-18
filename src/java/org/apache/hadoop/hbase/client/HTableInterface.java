begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HTableDescriptor
import|;
end_import

begin_comment
comment|/**  * Used to communicate with a single HBase table.  *  * @since 0.21.0  */
end_comment

begin_interface
specifier|public
interface|interface
name|HTableInterface
block|{
comment|/**    * Gets the name of this table.    *    * @return the table name.    */
name|byte
index|[]
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Gets the table descriptor for this table.    *    * @return table metadata    * @throws IOException    */
name|HTableDescriptor
name|getTableDescriptor
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Test for the existence of columns in the table, as specified in the Get.    *<p>    *    * This will return true if the Get matches one or more keys, false if not.    *<p>    *    * This is a server-side call so it prevents any data from being transfered to    * the client.    *    * @param get the Get    * @return true if the specified Get matches one or more keys, false if not    * @throws IOException    */
name|boolean
name|exists
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Method for getting data from a row.    *    * @param get the Get to fetch    * @return the result    * @throws IOException    */
name|Result
name|get
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Return the row that matches<i>row</i> and<i>family</i> exactly, or the    * one that immediately precedes it.    *    * @param row row key    * @param family Column family to look for row in    * @return map of values    * @throws IOException    */
name|Result
name|getRowOrBefore
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get a scanner on the current table as specified by the {@link Scan} object.    *    * @param scan a configured {@link Scan} object    * @return scanner    * @throws IOException    */
name|ResultScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get a scanner on the current table as specified by the {@link Scan} object.    *    * @param family the column family to scan    * @return the scanner    * @throws IOException    */
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get a scanner on the current table as specified by the {@link Scan} object.    *    * @param family the column family to scan    * @param qualifier the column qualifier to scan    * @return The scanner    * @throws IOException    */
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Commit a Put to the table.    *<p>    * If autoFlush is false, the update is buffered.    *    * @param put    * @throws IOException    */
name|void
name|put
parameter_list|(
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Commit a List of Puts to the table.    *<p>    * If autoFlush is false, the update is buffered.    *    * @param puts    * @throws IOException    */
name|void
name|put
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically checks if a row/family/qualifier value matches the expected    * value. If it does, it adds the put.    *    * @param row    * @param family    * @param qualifier    * @param value the expected value    * @param put    * @throws IOException    * @return true if the new put was executed, false otherwise    */
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes as specified by the delete.    *    * @param delete    * @throws IOException    */
name|void
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Atomically increments a column value. If the column value already exists    * and is not a big-endian long, this could throw an exception.    *    * @param row    * @param family    * @param qualifier    * @param amount    * @return the new value    * @throws IOException    */
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the value of autoFlush. If true, updates will not be buffered.    *    * @return true if autoFlush is enabled for this table    */
name|boolean
name|isAutoFlush
parameter_list|()
function_decl|;
comment|/**    * Flushes buffer data. Called automatically when autoFlush is true.    *    * @throws IOException    */
name|void
name|flushCommits
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Releases held resources.    *    * @throws IOException    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Obtains a row lock.    *    * @param row the row to lock    * @return rowLock RowLock containing row and lock id    * @throws IOException    */
name|RowLock
name|lockRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Releases the row lock.    *    * @param rl the row lock to release    * @throws IOException    */
name|void
name|unlockRow
parameter_list|(
name|RowLock
name|rl
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

