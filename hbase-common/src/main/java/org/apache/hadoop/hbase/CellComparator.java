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
name|yetus
operator|.
name|audience
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Comparator for comparing cells and has some specialized methods that allows comparing individual  * cell components like row, family, qualifier and timestamp  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|CellComparator
extends|extends
name|Comparator
argument_list|<
name|Cell
argument_list|>
block|{
comment|/**    * Lexographically compares two cells. The key part of the cell is taken for comparison which    * includes row, family, qualifier, timestamp and type    * @param leftCell the left hand side cell    * @param rightCell the right hand side cell    * @return greater than 0 if leftCell is bigger, less than 0 if rightCell is bigger, 0 if both    *         cells are equal    */
annotation|@
name|Override
name|int
name|compare
parameter_list|(
name|Cell
name|leftCell
parameter_list|,
name|Cell
name|rightCell
parameter_list|)
function_decl|;
comment|/**    * Lexographically compares the rows of two cells.    * @param leftCell the left hand side cell    * @param rightCell the right hand side cell    * @return greater than 0 if leftCell is bigger, less than 0 if rightCell is bigger, 0 if both    *         cells are equal    */
name|int
name|compareRows
parameter_list|(
name|Cell
name|leftCell
parameter_list|,
name|Cell
name|rightCell
parameter_list|)
function_decl|;
comment|/**    * Compares the row part of the cell with a simple plain byte[] like the    * stopRow in Scan.    * @param cell the cell    * @param bytes the byte[] representing the row to be compared with    * @param offset the offset of the byte[]    * @param length the length of the byte[]    * @return greater than 0 if leftCell is bigger, less than 0 if rightCell is bigger, 0 if both    *         cells are equal    */
name|int
name|compareRows
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**    * Lexographically compares the two cells excluding the row part. It compares family, qualifier,    * timestamp and the type    * @param leftCell the left hand side cell    * @param rightCell the right hand side cell    * @return greater than 0 if leftCell is bigger, less than 0 if rightCell is bigger, 0 if both    *         cells are equal    */
name|int
name|compareWithoutRow
parameter_list|(
name|Cell
name|leftCell
parameter_list|,
name|Cell
name|rightCell
parameter_list|)
function_decl|;
comment|/**    * Lexographically compares the families of the two cells    * @param leftCell the left hand side cell    * @param rightCell the right hand side cell    * @return greater than 0 if leftCell is bigger, less than 0 if rightCell is bigger, 0 if both    *         cells are equal    */
name|int
name|compareFamilies
parameter_list|(
name|Cell
name|leftCell
parameter_list|,
name|Cell
name|rightCell
parameter_list|)
function_decl|;
comment|/**    * Lexographically compares the qualifiers of the two cells    * @param leftCell the left hand side cell    * @param rightCell the right hand side cell    * @return greater than 0 if leftCell is bigger, less than 0 if rightCell is bigger, 0 if both    *         cells are equal    */
name|int
name|compareQualifiers
parameter_list|(
name|Cell
name|leftCell
parameter_list|,
name|Cell
name|rightCell
parameter_list|)
function_decl|;
comment|/**    * Compares cell's timestamps in DESCENDING order. The below older timestamps sorting ahead of    * newer timestamps looks wrong but it is intentional. This way, newer timestamps are first found    * when we iterate over a memstore and newer versions are the first we trip over when reading from    * a store file.    * @param leftCell the left hand side cell    * @param rightCell the right hand side cell    * @return 1 if left's timestamp&lt; right's timestamp -1 if left's timestamp&gt; right's    *         timestamp 0 if both timestamps are equal    */
name|int
name|compareTimestamps
parameter_list|(
name|Cell
name|leftCell
parameter_list|,
name|Cell
name|rightCell
parameter_list|)
function_decl|;
comment|/**    * Compares cell's timestamps in DESCENDING order. The below older timestamps sorting ahead of    * newer timestamps looks wrong but it is intentional. This way, newer timestamps are first found    * when we iterate over a memstore and newer versions are the first we trip over when reading from    * a store file.    * @param leftCellts the left cell's timestamp    * @param rightCellts the right cell's timestamp    * @return 1 if left's timestamp&lt; right's timestamp -1 if left's timestamp&gt; right's    *         timestamp 0 if both timestamps are equal    */
name|int
name|compareTimestamps
parameter_list|(
name|long
name|leftCellts
parameter_list|,
name|long
name|rightCellts
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

