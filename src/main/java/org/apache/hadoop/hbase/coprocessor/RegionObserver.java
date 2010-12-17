begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|KeyValue
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
name|Delete
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
name|Get
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
name|Result
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
name|Scan
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
name|Increment
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
name|coprocessor
operator|.
name|CoprocessorEnvironment
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
name|InternalScanner
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Coprocessors implement this interface to observe and mediate client actions  * on the region.  */
end_comment

begin_interface
specifier|public
interface|interface
name|RegionObserver
extends|extends
name|Coprocessor
block|{
comment|/**    * Called before the region is reported as open to the master.    * @param e the environment provided by the region server    */
specifier|public
name|void
name|preOpen
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|)
function_decl|;
comment|/**    * Called after the region is reported as open to the master.    * @param e the environment provided by the region server    */
specifier|public
name|void
name|postOpen
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|)
function_decl|;
comment|/**    * Called before the memstore is flushed to disk.    * @param e the environment provided by the region server    */
specifier|public
name|void
name|preFlush
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|)
function_decl|;
comment|/**    * Called after the memstore is flushed to disk.    * @param e the environment provided by the region server    */
specifier|public
name|void
name|postFlush
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|)
function_decl|;
comment|/**    * Called before compaction.    * @param e the environment provided by the region server    * @param willSplit true if compaction will result in a split, false    * otherwise    */
specifier|public
name|void
name|preCompact
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|boolean
name|willSplit
parameter_list|)
function_decl|;
comment|/**    * Called after compaction.    * @param e the environment provided by the region server    * @param willSplit true if compaction will result in a split, false    * otherwise    */
specifier|public
name|void
name|postCompact
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|boolean
name|willSplit
parameter_list|)
function_decl|;
comment|/**    * Called before the region is split.    * @param e the environment provided by the region server    * (e.getRegion() returns the parent region)    */
specifier|public
name|void
name|preSplit
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|)
function_decl|;
comment|/**    * Called after the region is split.    * @param e the environment provided by the region server    * (e.getRegion() returns the parent region)    * @param l the left daughter region    * @param r the right daughter region    */
specifier|public
name|void
name|postSplit
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|HRegion
name|l
parameter_list|,
specifier|final
name|HRegion
name|r
parameter_list|)
function_decl|;
comment|/**    * Called before the region is reported as closed to the master.    * @param e the environment provided by the region server    * @param abortRequested true if the region server is aborting    */
specifier|public
name|void
name|preClose
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
name|boolean
name|abortRequested
parameter_list|)
function_decl|;
comment|/**    * Called after the region is reported as closed to the master.    * @param e the environment provided by the region server    * @param abortRequested true if the region server is aborting    */
specifier|public
name|void
name|postClose
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
name|boolean
name|abortRequested
parameter_list|)
function_decl|;
comment|/**    * Called before a client makes a GetClosestRowBefore request.    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row the row    * @param family the family    * @param result The result to return to the client if default processing    * is bypassed. Can be modified. Will not be used if default processing    * is not bypassed.    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|preGetClosestRowBefore
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after a client makes a GetClosestRowBefore request.    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row the row    * @param family the desired family    * @param result the result to return to the client, modify as necessary    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|postGetClosestRowBefore
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before the client performs a Get    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param get the Get request    * @param result The result to return to the client if default processing    * is bypassed. Can be modified. Will not be used if default processing    * is not bypassed.    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|preGet
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the client performs a Get    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param get the Get request    * @param result the result to return to the client, modify as necessary    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|postGet
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before the client tests for existence using a Get.    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param get the Get request    * @param exists    * @return the value to return to the client if bypassing default processing    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|preExists
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|boolean
name|exists
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the client tests for existence using a Get.    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param get the Get request    * @param exists the result returned by the region server    * @return the result to return to the client    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|postExists
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|boolean
name|exists
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before the client stores a value.    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param familyMap map of family to edits for the given family    * @param writeToWAL true if the change should be written to the WAL    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the client stores a value.    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param familyMap map of family to edits for the given family    * @param writeToWAL true if the change should be written to the WAL    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|postPut
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before the client deletes a value.    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param familyMap map of family to edits for the given family    * @param writeToWAL true if the change should be written to the WAL    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|preDelete
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the client deletes a value.    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param familyMap map of family to edits for the given family    * @param writeToWAL true if the change should be written to the WAL    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|postDelete
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before checkAndPut    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param value the expected value    * @param put data to put if check succeeds    * @param result     * @return the return value to return to client if bypassing default    * processing    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|preCheckAndPut
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after checkAndPut    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param value the expected value    * @param put data to put if check succeeds    * @param result from the checkAndPut    * @return the possibly transformed return value to return to client    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|postCheckAndPut
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before checkAndDelete    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param value the expected value    * @param delete delete to commit if check succeeds    * @param result     * @return the value to return to client if bypassing default processing    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|preCheckAndDelete
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after checkAndDelete    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param value the expected value    * @param delete delete to commit if check succeeds    * @param result from the CheckAndDelete    * @return the possibly transformed returned value to return to client    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|postCheckAndDelete
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before incrementColumnValue    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param amount long amount to increment    * @param writeToWAL true if the change should be written to the WAL    * @return value to return to the client if bypassing default processing    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|long
name|preIncrementColumnValue
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|long
name|amount
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after incrementColumnValue    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param row row to check    * @param family column family    * @param qualifier column qualifier    * @param amount long amount to increment    * @param writeToWAL true if the change should be written to the WAL    * @param result the result returned by incrementColumnValue    * @return the result to return to the client    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|long
name|postIncrementColumnValue
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|long
name|amount
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|,
specifier|final
name|long
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before Increment    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param increment increment object    * @param result The result to return to the client if default processing    * is bypassed. Can be modified. Will not be used if default processing    * is not bypassed.    * @param writeToWAL true if the change should be written to the WAL    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|preIncrement
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Increment
name|increment
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after increment    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param increment increment object    * @param writeToWAL true if the change should be written to the WAL    * @param result the result returned by increment, can be modified    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|postIncrement
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Increment
name|increment
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before the client opens a new scanner.    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param scan the Scan specification    * @param s if not null, the base scanner    * @return an InternalScanner instance to use instead of the base scanner if    * overriding default behavior, null otherwise    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|InternalScanner
name|preScannerOpen
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the client opens a new scanner.    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param scan the Scan specification    * @param s if not null, the base scanner    * @return the scanner instance to use    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|InternalScanner
name|postScannerOpen
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before the client asks for the next row on a scanner.    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param s the scanner    * @param result The result to return to the client if default processing    * is bypassed. Can be modified. Will not be returned if default processing    * is not bypassed.    * @param limit the maximum number of results to return    * @param hasNext the 'has more' indication    * @return 'has more' indication that should be sent to client    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|preScannerNext
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|,
specifier|final
name|int
name|limit
parameter_list|,
specifier|final
name|boolean
name|hasNext
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the client asks for the next row on a scanner.    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param s the scanner    * @param result the result to return to the client, can be modified    * @param limit the maximum number of results to return    * @param hasNext the 'has more' indication    * @return 'has more' indication that should be sent to client    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|boolean
name|postScannerNext
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
parameter_list|,
specifier|final
name|int
name|limit
parameter_list|,
specifier|final
name|boolean
name|hasNext
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before the client closes a scanner.    *<p>    * Call CoprocessorEnvironment#bypass to skip default actions    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param s the scanner    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|preScannerClose
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the client closes a scanner.    *<p>    * Call CoprocessorEnvironment#complete to skip any subsequent chained    * coprocessors    * @param e the environment provided by the region server    * @param s the scanner    * @throws IOException if an error occurred on the coprocessor    */
specifier|public
name|void
name|postScannerClose
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

