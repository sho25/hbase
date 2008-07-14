begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (c) 2005, European Commission project OneLab under contract 034819  * (http://www.one-lab.org)  *   * All rights reserved.  * Redistribution and use in source and binary forms, with or   * without modification, are permitted provided that the following   * conditions are met:  *  - Redistributions of source code must retain the above copyright   *    notice, this list of conditions and the following disclaimer.  *  - Redistributions in binary form must reproduce the above copyright   *    notice, this list of conditions and the following disclaimer in   *    the documentation and/or other materials provided with the distribution.  *  - Neither the name of the University Catholique de Louvain - UCL  *    nor the names of its contributors may be used to endorse or   *    promote products derived from this software without specific prior   *    written permission.  *      * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT   * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE   * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,   * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,   * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;   * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER   * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT   * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN   * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE   * POSSIBILITY OF SUCH DAMAGE.  */
end_comment

begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|onelab
operator|.
name|test
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|onelab
operator|.
name|filter
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Test class.  *   * contract<a href="http://www.one-lab.org">European Commission One-Lab Project 034819</a>.  *  * @version 1.0 - 8 Feb. 07  */
end_comment

begin_class
specifier|public
class|class
name|TestFilter
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestFilter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Test a BloomFilter    * @throws UnsupportedEncodingException    * @throws IOException    */
specifier|public
name|void
name|testBloomFilter
parameter_list|()
throws|throws
name|UnsupportedEncodingException
throws|,
name|IOException
block|{
specifier|final
name|StringKey
index|[]
name|inserted
init|=
block|{
operator|new
name|StringKey
argument_list|(
literal|"wmjwjzyv"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"baietibz"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"guhsgxnv"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"mhnqycto"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"xcyqafgz"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"zidoamgb"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"tftfirzd"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"okapqlrg"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"yccwzwsq"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"qmonufqu"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"wlsctews"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"mksdhqri"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"wxxllokj"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"eviuqpls"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"bavotqmj"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"yibqzhdl"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"csfqmsyr"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"guxliyuh"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"pzicietj"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"qdwgrqwo"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ujfzecmi"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"dzeqfvfi"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"phoegsij"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"bvudfcou"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"dowzmciz"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"etvhkizp"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"rzurqycg"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"krqfxuge"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"gflcohtd"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"fcrcxtps"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"qrtovxdq"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"aypxwrwi"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"dckpyznr"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"mdaawnpz"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"pakdfvca"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"xjglfbez"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"xdsecofi"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"sjlrfcab"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ebcjawxv"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"hkafkjmy"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"oimmwaxo"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"qcuzrazo"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"nqydfkwk"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"frybvmlb"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"amxmaqws"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"gtkovkgx"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"vgwxrwss"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"xrhzmcep"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"tafwziil"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"erjmncnv"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"heyzqzrn"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"sowvyhtu"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"heeixgzy"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ktcahcob"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ljhbybgg"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"jiqfcksl"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"anjdkjhm"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"uzcgcuxp"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"vzdhjqla"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"svhgwwzq"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"zhswvhbp"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ueceybwy"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"czkqykcw"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ctisayir"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"hppbgciu"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"nhzgljfk"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"vaziqllf"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"narvrrij"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"kcevbbqi"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"qymuaqnp"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"pwqpfhsr"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"peyeicuk"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"kudlwihi"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"pkmqejlm"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ylwzjftl"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"rhqrlqar"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"xmftvzsp"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"iaemtihk"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ymsbrqcu"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"yfnlcxto"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"nluqopqh"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"wmrzhtox"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"qnffhqbl"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"zypqpnbw"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"oiokhatd"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"mdraddiu"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"zqoatltt"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ewhulbtm"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"nmswpsdf"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"xsjeteqe"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ufubcbma"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"phyxvrds"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"vhnfldap"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"zrrlycmg"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"becotcjx"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"wvbubokn"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"avkgiopr"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"mbqqxmrv"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ibplgvuu"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"dghvpkgc"
argument_list|)
block|}
decl_stmt|;
specifier|final
name|StringKey
index|[]
name|notInserted
init|=
block|{
operator|new
name|StringKey
argument_list|(
literal|"abcdefgh"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"ijklmnop"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"qrstuvwx"
argument_list|)
block|,
operator|new
name|StringKey
argument_list|(
literal|"yzabcdef"
argument_list|)
block|}
decl_stmt|;
comment|/*       * Bloom filters are very sensitive to the number of elements inserted into      * them.      *       * If m denotes the number of bits in the Bloom filter (vectorSize),      * n denotes the number of elements inserted into the Bloom filter and      * k represents the number of hash functions used (nbHash), then      * according to Broder and Mitzenmacher,      *       * ( http://www.eecs.harvard.edu/~michaelm/NEWWORK/postscripts/BloomFilterSurvey.pdf )      *       * the probability of false positives is minimized when k is      * approximately ln(2) * m/n.      *       * If we fix the number of hash functions and know the number of entries,      * then the optimal vector size m = (k * n) / ln(2)      */
specifier|final
name|int
name|DEFAULT_NUMBER_OF_HASH_FUNCTIONS
init|=
literal|4
decl_stmt|;
name|BloomFilter
name|bf
init|=
operator|new
name|BloomFilter
argument_list|(
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|DEFAULT_NUMBER_OF_HASH_FUNCTIONS
operator|*
operator|(
literal|1.0
operator|*
name|inserted
operator|.
name|length
operator|)
operator|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2.0
argument_list|)
argument_list|)
argument_list|,
name|DEFAULT_NUMBER_OF_HASH_FUNCTIONS
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inserted
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|bf
operator|.
name|add
argument_list|(
name|inserted
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Verify that there are no false negatives and few (if any) false positives
name|checkFalsePositivesNegatives
argument_list|(
name|bf
argument_list|,
name|inserted
argument_list|,
name|notInserted
argument_list|)
expr_stmt|;
comment|// Test serialization/deserialization
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking serialization/deserialization"
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|out
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|bf
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|bf
operator|=
operator|new
name|BloomFilter
argument_list|()
expr_stmt|;
name|bf
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
comment|// Verify that there are no false negatives and few (if any) false positives
name|checkFalsePositivesNegatives
argument_list|(
name|bf
argument_list|,
name|inserted
argument_list|,
name|notInserted
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkFalsePositivesNegatives
parameter_list|(
name|BloomFilter
name|bf
parameter_list|,
name|StringKey
index|[]
name|inserted
parameter_list|,
name|StringKey
index|[]
name|notInserted
parameter_list|)
block|{
comment|// Test membership for values we inserted. Should not get false negatives
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking for false negatives"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inserted
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|bf
operator|.
name|membershipTest
argument_list|(
name|inserted
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"false negative for: "
operator|+
name|inserted
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Test membership for values we did not insert. It is possible to get
comment|// false positives
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking for false positives"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|notInserted
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|bf
operator|.
name|membershipTest
argument_list|(
name|notInserted
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"false positive for: "
operator|+
name|notInserted
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Success!"
argument_list|)
expr_stmt|;
block|}
comment|/** Test a CountingBloomFilter    * @throws UnsupportedEncodingException    */
specifier|public
name|void
name|testCountingBloomFilter
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
name|Filter
name|bf
init|=
operator|new
name|CountingBloomFilter
argument_list|(
literal|8
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|Key
name|key
init|=
operator|new
name|StringKey
argument_list|(
literal|"toto"
argument_list|)
decl_stmt|;
name|Key
name|k2
init|=
operator|new
name|StringKey
argument_list|(
literal|"lulu"
argument_list|)
decl_stmt|;
name|Key
name|k3
init|=
operator|new
name|StringKey
argument_list|(
literal|"mama"
argument_list|)
decl_stmt|;
name|bf
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|bf
operator|.
name|add
argument_list|(
name|k2
argument_list|)
expr_stmt|;
name|bf
operator|.
name|add
argument_list|(
name|k3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"graknyl"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"xyzzy"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"abcd"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// delete 'key', and check that it is no longer a member
operator|(
operator|(
name|CountingBloomFilter
operator|)
name|bf
operator|)
operator|.
name|delete
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
comment|// OR 'key' back into the filter
name|Filter
name|bf2
init|=
operator|new
name|CountingBloomFilter
argument_list|(
literal|8
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|bf2
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|bf
operator|.
name|or
argument_list|(
name|bf2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"graknyl"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"xyzzy"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"abcd"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// to test for overflows, add 'key' enough times to overflow an 8bit bucket,
comment|// while asserting that it stays a member
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|257
condition|;
name|i
operator|++
control|)
block|{
name|bf
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Test a DynamicBloomFilter    * @throws UnsupportedEncodingException    */
specifier|public
name|void
name|testDynamicBloomFilter
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
name|Filter
name|bf
init|=
operator|new
name|DynamicBloomFilter
argument_list|(
literal|8
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|Key
name|key
init|=
operator|new
name|StringKey
argument_list|(
literal|"toto"
argument_list|)
decl_stmt|;
name|Key
name|k2
init|=
operator|new
name|StringKey
argument_list|(
literal|"lulu"
argument_list|)
decl_stmt|;
name|Key
name|k3
init|=
operator|new
name|StringKey
argument_list|(
literal|"mama"
argument_list|)
decl_stmt|;
name|bf
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|bf
operator|.
name|add
argument_list|(
name|k2
argument_list|)
expr_stmt|;
name|bf
operator|.
name|add
argument_list|(
name|k3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"graknyl"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"xyzzy"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf
operator|.
name|membershipTest
argument_list|(
operator|new
name|StringKey
argument_list|(
literal|"abcd"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_comment
comment|//end class
end_comment

end_unit

