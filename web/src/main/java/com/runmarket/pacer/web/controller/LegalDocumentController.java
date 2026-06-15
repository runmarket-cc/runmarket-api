package com.runmarket.pacer.web.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이용약관 / 개인정보처리방침 등 법적 고지 문서를 인증 없이(permitAll) 제공한다.
 * 브라우저에서 바로 열람할 수 있도록 자체 완결형(self-contained) HTML 페이지로 응답한다.
 * 문구는 코드 내 정적 상수로 관리한다.
 */
@RestController
public class LegalDocumentController {

    /** 프론트 홈으로 돌아가는 링크 */
    private static final String HOME_URL = "https://www.runmarket.cc/";

    private static final String SERVICE_NAME = "런마켓(RunMarket)";
    private static final String CONTACT_EMAIL = "gudrb963@gmail.com";
    private static final String EFFECTIVE_DATE = "2026년 6월 10일";

    private static final String STYLE = """
            <style>
              * { box-sizing: border-box; }
              body {
                margin: 0;
                padding: 2rem 1rem 5rem;
                background: #f3f4f6;
                color: #111827;
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
                  "Helvetica Neue", "Apple SD Gothic Neo", "Malgun Gothic", sans-serif;
                line-height: 1.7;
                font-size: 14px;
              }
              .wrap { max-width: 768px; margin: 0 auto; }
              .brand { font-size: 1.5rem; font-weight: 800; color: #111827; margin-bottom: 1.5rem; }
              .brand span { color: #2563eb; }
              article {
                background: #fff;
                border: 1px solid #d1d5db;
                border-radius: 0.5rem;
                box-shadow: 0 1px 2px rgba(0,0,0,0.05);
                padding: 2rem 1.5rem;
              }
              @media (min-width: 640px) { article { padding: 2.5rem; } }
              h1 { font-size: 1.5rem; font-weight: 700; margin: 0 0 0.5rem; }
              .effective { font-size: 0.875rem; color: #6b7280; margin: 0 0 2rem; }
              section { margin-bottom: 2rem; }
              h2 { font-size: 1rem; font-weight: 600; margin: 0 0 0.5rem; }
              p { color: #6b7280; margin: 0 0 0.5rem; }
              ol, ul { color: #6b7280; padding-left: 1.25rem; margin: 0; }
              ol { list-style: decimal; }
              ul { list-style: disc; }
              li { margin-bottom: 0.25rem; }
              li ul, li ol { margin-top: 0.25rem; }
              strong { color: #111827; }
              .label { font-weight: 500; color: #111827; }
              a { color: #2563eb; }
              table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.75rem; color: #6b7280; }
              th { font-weight: 600; color: #111827; border-bottom: 1px solid #d1d5db; padding: 0.5rem 1rem 0.5rem 0; }
              td { border-bottom: 1px solid #e5e7eb; padding: 0.5rem 1rem 0.5rem 0; }
              .footer { display: flex; justify-content: space-between; font-size: 0.875rem; margin-top: 1.5rem; }
              .footer a { font-weight: 500; }
              .border-top { border-top: 1px solid #e5e7eb; padding-top: 1.5rem; }
            </style>
            """;

    @GetMapping(value = "/terms", produces = MediaType.TEXT_HTML_VALUE)
    public String terms() {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>이용약관 - 런마켓</title>
                <meta name="description" content="런마켓 서비스 이용약관">
                """ + STYLE + """
                </head>
                <body>
                <div class="wrap">
                  <div class="brand">Run<span>market</span></div>
                  <article>
                    <h1>이용약관</h1>
                    <p class="effective">시행일: """ + EFFECTIVE_DATE + """
                    </p>

                    <section>
                      <h2>제1조 (목적)</h2>
                      <p>이 약관은 """ + SERVICE_NAME + """
                      (이하 &ldquo;서비스&rdquo;)을 운영하는 운영자(이하 &ldquo;운영자&rdquo;)와 이용자 간의 권리·의무 및
                      책임사항, 서비스 이용조건 및 절차 등 기본적인 사항을 규정함을 목적으로 합니다.</p>
                    </section>

                    <section>
                      <h2>제2조 (정의)</h2>
                      <p>이 약관에서 사용하는 용어의 정의는 다음과 같습니다.</p>
                      <ol>
                        <li>&ldquo;서비스&rdquo;란 운영자가 제공하는 마라톤·러닝 등 각종 대회 정보의 검색·조회, 관심 대회 저장(찜) 등 일체의 서비스를 의미합니다.</li>
                        <li>&ldquo;이용자&rdquo;란 이 약관에 따라 서비스를 이용하는 회원 및 비회원을 말합니다.</li>
                        <li>&ldquo;회원&rdquo;이란 이메일을 제공하여 회원등록을 한 자로서, 서비스를 계속적으로 이용할 수 있는 자를 말합니다.</li>
                        <li>&ldquo;대회 주최자&rdquo;란 서비스에 게재된 대회를 실제로 개최·운영하며, 참가 신청·접수·결제를 직접 처리하는 제3의 주체를 말합니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제3조 (약관의 게시와 개정)</h2>
                      <ol>
                        <li>운영자는 이 약관의 내용을 이용자가 쉽게 알 수 있도록 서비스 화면을 통하여 게시합니다.</li>
                        <li>운영자는 관련 법령을 위배하지 않는 범위에서 이 약관을 개정할 수 있으며, 개정 시 적용일자 및 개정사유를 명시하여 적용일자 7일 전부터(이용자에게 불리한 변경의 경우 30일 전부터) 서비스 화면에 공지합니다.</li>
                        <li>이용자가 개정 약관에 동의하지 않는 경우 이용계약을 해지(회원 탈퇴)할 수 있으며, 공지된 적용일자 이후에도 서비스를 계속 이용하는 경우 개정 약관에 동의한 것으로 봅니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제4조 (회원가입 및 이용계약의 성립)</h2>
                      <ol>
                        <li>이용계약은 이용자가 이 약관 및 개인정보처리방침에 동의하고 이메일·비밀번호를 입력한 후 이메일 인증을 완료함으로써 성립합니다.</li>
                        <li>운영자는 다음 각 호에 해당하는 경우 가입 신청을 승낙하지 않거나 사후에 이용계약을 해지할 수 있습니다.
                          <ul>
                            <li>타인의 정보를 도용하거나 허위의 정보를 기재한 경우</li>
                            <li>이미 가입된 이메일로 중복하여 가입을 신청한 경우</li>
                            <li>관련 법령을 위반하거나 서비스 운영을 방해할 목적으로 신청한 경우</li>
                          </ul>
                        </li>
                      </ol>
                    </section>

                    <section>
                      <h2>제5조 (회원 정보의 관리)</h2>
                      <ol>
                        <li>회원은 이메일·비밀번호 등 계정 정보를 선량한 관리자의 주의로 관리하여야 합니다.</li>
                        <li>회원은 자신의 계정 정보를 제3자에게 양도하거나 대여할 수 없으며, 계정이 도용되었음을 인지한 경우 즉시 운영자에게 통지하고 안내에 따라야 합니다.</li>
                        <li>제2항의 통지를 하지 않거나 통지 후 운영자의 안내에 따르지 않아 발생한 불이익에 대하여 운영자는 책임을 지지 않습니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제6조 (서비스의 내용)</h2>
                      <ol>
                        <li>운영자는 이용자에게 마라톤·러닝 등 대회의 명칭, 일정, 장소, 종목(거리), 접수 상태, 공식 홈페이지 링크 등 대회 관련 정보를 검색·조회할 수 있는 서비스를 제공합니다.</li>
                        <li>회원은 관심 있는 대회를 저장(찜)하고 마이페이지에서 이를 조회할 수 있습니다.</li>
                        <li><strong>운영자는 대회 정보를 수집·정리하여 제공하는 정보 매개자일 뿐, 서비스에 게재된 대회를 직접 주최·운영하지 않습니다.</strong> 대회 참가 신청·접수·결제·환불 등은 각 대회 주최자가 운영하는 공식 홈페이지 등 외부 채널에서 이루어지며, 운영자는 그 거래의 당사자가 아닙니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제7조 (서비스의 제공 및 변경·중단)</h2>
                      <ol>
                        <li>서비스는 연중무휴, 1일 24시간 제공함을 원칙으로 합니다.</li>
                        <li>운영자는 다음 각 호의 경우 서비스의 전부 또는 일부를 제한하거나 중단할 수 있습니다.
                          <ul>
                            <li>설비의 보수·점검 또는 시스템 장애가 발생한 경우</li>
                            <li>천재지변, 정전, 통신망 두절 등 불가항력적 사유가 있는 경우</li>
                            <li>외부 서비스 제공자(호스팅·인증 등)의 장애가 있는 경우</li>
                            <li>그 밖에 운영자의 운영상·기술상 필요에 의한 경우</li>
                          </ul>
                        </li>
                        <li>서비스는 운영자의 사정에 따라 변경되거나 종료될 수 있으며, 이 경우 운영자는 사전에 공지하도록 노력합니다. 다만 무료로 제공되는 서비스의 특성상 부득이한 경우 사후에 공지할 수 있습니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제8조 (이용자의 의무)</h2>
                      <p>이용자는 다음 각 호의 행위를 하여서는 안 됩니다.</p>
                      <ol>
                        <li>타인의 정보를 도용하거나 허위 사실을 등록하는 행위</li>
                        <li>서비스에 게재된 정보를 무단으로 복제·배포하거나 상업적으로 이용하는 행위</li>
                        <li>자동화된 수단(봇, 크롤러, 스크래퍼 등)을 이용하여 서비스에 비정상적으로 접근하거나 대량의 정보를 수집하는 행위</li>
                        <li>운영자 및 제3자의 지식재산권, 명예, 그 밖의 권리를 침해하는 행위</li>
                        <li>서비스의 안정적 운영을 방해하는 행위</li>
                        <li>그 밖에 관련 법령 또는 이 약관에 위배되는 행위</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제9조 (저작권 및 지식재산권)</h2>
                      <ol>
                        <li>서비스에 게재된 디자인, 상표, 로고, 소프트웨어 및 운영자가 정리·제공한 정보에 대한 저작권 및 지식재산권은 운영자 또는 정당한 권리자에게 귀속됩니다.</li>
                        <li>이용자는 운영자의 사전 동의 없이 제1항의 정보를 복제·전송·배포 기타 방법으로 영리 목적에 이용하거나 제3자에게 이용하게 하여서는 안 됩니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제10조 (이용계약의 해지 및 이용제한)</h2>
                      <ol>
                        <li>회원은 언제든지 마이페이지의 회원 탈퇴 기능을 통하여 이용계약을 해지할 수 있으며, 탈퇴 시 회원의 개인정보는 개인정보처리방침에 따라 지체 없이 파기됩니다.</li>
                        <li>회원이 이 약관 또는 관련 법령을 위반하는 경우, 운영자는 서비스 이용을 제한하거나 이용계약을 해지할 수 있습니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제11조 (책임의 제한)</h2>
                      <ol>
                        <li>운영자는 서비스에 게재된 대회 정보의 정확성·완전성·최신성을 확보하기 위하여 노력하나, 해당 정보는 대회 주최자가 제공·변경하는 내용에 기초합니다. 따라서 대회의 일정·장소·종목·참가비·접수 상태 등이 실제와 다를 수 있으며, <strong>이용자는 참가 신청 전 반드시 각 대회 공식 홈페이지에서 최종 정보를 확인하여야 합니다.</strong></li>
                        <li>운영자는 대회의 개최·취소·연기·변경, 참가 신청·결제·환불, 대회 진행 중 발생한 사고나 분쟁 등 대회 주최자와 이용자 간에 발생하는 일체의 문제에 대하여 책임을 지지 않습니다.</li>
                        <li>운영자는 천재지변, 불가항력, 이용자의 귀책사유, 제3자(외부 서비스 제공자 포함)의 귀책사유로 서비스를 제공할 수 없는 경우 그에 대한 책임이 면제됩니다.</li>
                        <li>서비스는 무료로 제공되며, 운영자는 관련 법령에 특별한 규정이 없는 한 무료 서비스의 이용과 관련하여 책임을 지지 않습니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제12조 (준거법 및 재판관할)</h2>
                      <ol>
                        <li>운영자와 이용자 간에 발생한 분쟁에 관하여는 대한민국 법령을 준거법으로 합니다.</li>
                        <li>서비스 이용과 관련한 분쟁에 관한 소송은 「민사소송법」에 따른 관할 법원을 제1심 관할 법원으로 합니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제13조 (문의)</h2>
                      <p>서비스 이용과 관련한 문의는 아래 이메일로 연락하실 수 있습니다.<br>
                      <a href="mailto:""" + CONTACT_EMAIL + "\">" + CONTACT_EMAIL + """
                      </a></p>
                    </section>

                    <section class="border-top">
                      <h2>부칙</h2>
                      <p>이 약관은 """ + EFFECTIVE_DATE + """
                      부터 시행합니다.</p>
                    </section>
                  </article>

                  <div class="footer">
                    <a href=\"""" + HOME_URL + """
                    ">← 홈으로</a>
                    <a href="/privacy">개인정보처리방침 →</a>
                  </div>
                </div>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/privacy", produces = MediaType.TEXT_HTML_VALUE)
    public String privacy() {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>개인정보처리방침 - 런마켓</title>
                <meta name="description" content="런마켓 개인정보처리방침">
                """ + STYLE + """
                </head>
                <body>
                <div class="wrap">
                  <div class="brand">Run<span>market</span></div>
                  <article>
                    <h1>개인정보처리방침</h1>
                    <p class="effective">시행일: """ + EFFECTIVE_DATE + """
                    </p>

                    <section>
                      <p>""" + SERVICE_NAME + """
                      (이하 &ldquo;서비스&rdquo;)의 운영자(이하 &ldquo;운영자&rdquo;)는 「개인정보 보호법」 등 관련 법령을 준수하며,
                      이용자의 개인정보를 최소한으로 수집합니다. 본 개인정보처리방침을 통하여 이용자의 개인정보가 어떠한 목적과
                      방식으로 처리되는지 알려드립니다.</p>
                    </section>

                    <section>
                      <h2>제1조 (수집하는 개인정보 항목 및 수집 방법)</h2>
                      <ol>
                        <li><span class="label">필수 수집 항목</span>: 이메일 주소
                          <ul>
                            <li>회원 식별, 로그인 및 이메일 인증을 위해 수집합니다.</li>
                            <li>비밀번호는 로그인 인증을 위해 입력받되 복호화할 수 없는 형태(일방향 암호화)로만 저장되며, 운영자가 원문을 알 수 없습니다.</li>
                          </ul>
                        </li>
                        <li><span class="label">서비스 이용 과정에서 생성되는 정보</span>: 관심 대회 저장(찜) 내역</li>
                        <li><span class="label">자동으로 수집되는 정보</span>: 서비스 보안 및 안정적 운영 과정에서 접속 IP 주소, 쿠키, 접속 일시 등이 자동으로 수집·처리될 수 있습니다.</li>
                      </ol>
                      <p>운영자는 위 항목 외에 이름, 전화번호, 주소, 결제정보 등 별도의 개인정보를 수집하지 않습니다. 수집 방법은 회원가입·서비스 이용 시 이용자가 직접 입력하거나, 서비스 이용 과정에서 자동으로 생성·수집되는 방식입니다.</p>
                    </section>

                    <section>
                      <h2>제2조 (개인정보의 처리 목적)</h2>
                      <p>운영자는 수집한 개인정보를 다음의 목적으로만 처리합니다.</p>
                      <ol>
                        <li>회원 식별 및 본인 확인, 회원제 서비스 제공</li>
                        <li>이메일 인증을 통한 가입 의사 확인 및 부정 가입 방지</li>
                        <li>관심 대회 저장(찜) 기능 제공</li>
                        <li>고객 문의 접수 및 처리</li>
                        <li>부정 이용 방지 및 서비스의 안정적 운영·보안</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제3조 (개인정보의 보유 및 이용 기간)</h2>
                      <ol>
                        <li>운영자는 이용자의 개인정보를 회원 탈퇴 시까지 보유하며, 회원 탈퇴 시 지체 없이 파기합니다.</li>
                        <li>다만 관련 법령에 따라 보존할 필요가 있는 경우 해당 법령에서 정한 기간 동안 보관합니다.
                          <ul>
                            <li>웹사이트 방문 기록(접속 로그, 접속 IP 등): 3개월 (통신비밀보호법)</li>
                          </ul>
                        </li>
                      </ol>
                    </section>

                    <section>
                      <h2>제4조 (개인정보의 제3자 제공)</h2>
                      <p>운영자는 이용자의 개인정보를 제3자에게 제공하지 않습니다. 다만 이용자가 사전에 동의하거나, 법령의 규정에 의하여 수사기관 등이 법령에 정해진 절차와 방법에 따라 요구하는 경우에는 예외로 합니다.</p>
                    </section>

                    <section>
                      <h2>제5조 (개인정보 처리의 위탁 및 국외 이전)</h2>
                      <p>운영자는 서비스 운영을 위하여 아래의 외부 서비스를 이용하고 있으며, 이 과정에서 접속 IP·쿠키 등 일부 정보가 해외 서버로 이전·처리될 수 있습니다.</p>
                      <table>
                        <thead>
                          <tr>
                            <th>제공자 (국가)</th>
                            <th>이용 목적</th>
                            <th>이전 항목</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr>
                            <td>Vercel Inc. (미국)</td>
                            <td>서비스 호스팅 및 이용 통계 분석</td>
                            <td>접속 IP, 쿠키, 접속 기록</td>
                          </tr>
                          <tr>
                            <td>Cloudflare, Inc. (미국)</td>
                            <td>봇 차단 등 보안 인증 및 네트워크 보안</td>
                            <td>접속 IP, 쿠키, 기기·브라우저 정보</td>
                          </tr>
                        </tbody>
                      </table>
                      <p>이전 일시 및 방법은 서비스 이용 시점에 정보통신망을 통하여 수시로 이전됩니다. 이용자는 운영자에게 연락하여 국외 이전을 거부할 수 있으나, 이 경우 회원가입 및 서비스 이용이 제한될 수 있습니다.</p>
                    </section>

                    <section>
                      <h2>제6조 (정보주체의 권리·의무 및 행사 방법)</h2>
                      <ol>
                        <li>이용자는 언제든지 자신의 개인정보에 대한 열람·정정·삭제·처리정지를 요구할 수 있습니다.</li>
                        <li>회원은 마이페이지의 회원 탈퇴 기능을 통하여 직접 회원 탈퇴(개인정보 삭제)를 할 수 있으며, 그 밖의 권리 행사는 아래 연락처로 요청할 수 있습니다. 운영자는 지체 없이 필요한 조치를 취합니다.</li>
                        <li>만 14세 미만 아동의 회원가입은 허용하지 않으며, 운영자는 만 14세 미만 아동의 개인정보를 수집하지 않습니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제7조 (개인정보의 파기 절차 및 방법)</h2>
                      <ol>
                        <li>운영자는 개인정보의 처리 목적이 달성되거나 보유 기간이 경과한 경우 지체 없이 해당 개인정보를 파기합니다.</li>
                        <li>전자적 파일 형태로 저장된 개인정보는 복구·재생이 불가능한 방법으로 영구 삭제합니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제8조 (개인정보의 안전성 확보 조치)</h2>
                      <p>운영자는 개인정보의 안전성 확보를 위하여 다음과 같은 조치를 취하고 있습니다.</p>
                      <ol>
                        <li>비밀번호의 일방향 암호화 저장 및 전송 구간 암호화(SSL/TLS) 적용</li>
                        <li>개인정보에 대한 접근 권한 관리 및 접근 통제</li>
                        <li>봇 차단 등 비인가 접근을 막기 위한 보안 인증 수단 운영</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제9조 (쿠키 등 자동 수집 장치의 운영 및 거부)</h2>
                      <ol>
                        <li>서비스는 로그인 유지 및 이용 통계 분석 등을 위하여 쿠키(cookie)를 사용할 수 있습니다.</li>
                        <li>이용자는 웹 브라우저 설정을 통하여 쿠키 저장을 거부할 수 있습니다. 다만 쿠키 저장을 거부하는 경우 로그인 유지 등 일부 서비스 이용에 어려움이 있을 수 있습니다.</li>
                      </ol>
                    </section>

                    <section>
                      <h2>제10조 (개인정보 보호책임자 및 문의)</h2>
                      <p>개인정보 처리에 관한 문의, 불만 처리, 피해 구제 등에 관한 사항은 아래로 연락하실 수 있습니다.</p>
                      <ul>
                        <li>개인정보 보호책임자: 런마켓 운영자</li>
                        <li>연락처: <a href="mailto:""" + CONTACT_EMAIL + "\">" + CONTACT_EMAIL + """
                        </a></li>
                      </ul>
                    </section>

                    <section>
                      <h2>제11조 (권익침해 구제 방법)</h2>
                      <p>이용자는 개인정보 침해로 인한 분쟁 해결이나 상담이 필요한 경우 아래 기관에 문의할 수 있습니다.</p>
                      <ul>
                        <li>개인정보 분쟁조정위원회: (국번 없이) 1833-6972 / www.kopico.go.kr</li>
                        <li>개인정보침해 신고센터: (국번 없이) 118 / privacy.kisa.or.kr</li>
                        <li>경찰청 사이버수사국: (국번 없이) 182 / ecrm.police.go.kr</li>
                      </ul>
                    </section>

                    <section class="border-top">
                      <h2>제12조 (개인정보처리방침의 변경)</h2>
                      <p>이 개인정보처리방침은 """ + EFFECTIVE_DATE + """
                      부터 적용됩니다. 내용이 추가·삭제·수정되는 경우 변경 사항의 시행 7일 전(이용자에게 불리한 변경의 경우 30일 전)부터
                      서비스 화면을 통하여 고지하겠습니다.</p>
                    </section>
                  </article>

                  <div class="footer">
                    <a href=\"""" + HOME_URL + """
                    ">← 홈으로</a>
                    <a href="/terms">이용약관 →</a>
                  </div>
                </div>
                </body>
                </html>
                """;
    }
}
