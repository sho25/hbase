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
comment|/**  * An interface for iterating through a sequence of cells. Similar to Java's Iterator, but without  * the hasNext() or remove() methods. The hasNext() method is problematic because it may require  * actually loading the next object, which in turn requires storing the previous object somewhere.  *  *<p>The core data block decoder should be as fast as possible, so we push the complexity and  * performance expense of concurrently tracking multiple cells to layers above the CellScanner.  *<p>  * The {@link #current()} method will return a reference to a Cell implementation. This reference  * may or may not point to a reusable cell implementation, so users of the CellScanner should not,  * for example, accumulate a List of Cells. All of the references may point to the same object,  * which would be the latest state of the underlying Cell. In short, the Cell is mutable.  *</p>  * Typical usage:  *  *<pre>  * while (scanner.advance()) {  *   Cell cell = scanner.current();  *   // do something  * }  *</pre>  *<p>Often used reading {@link org.apache.hadoop.hbase.Cell}s written by  * {@link org.apache.hadoop.hbase.io.CellOutputStream}.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|CellScanner
block|{
comment|/**    * @return the current Cell which may be mutable    */
name|Cell
name|current
parameter_list|()
function_decl|;
comment|/**    * Advance the scanner 1 cell.    * @return true if the next cell is found and {@link #current()} will return a valid Cell    * @throws IOException if advancing the scanner fails    */
name|boolean
name|advance
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

